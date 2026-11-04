package com.estonianport.agendaza.common.security

import com.estonianport.agendaza.repository.UsuarioRepository
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.LockedException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class JWTAuthenticationFilter(
    private val loginAttemptService: LoginAttemptService,
    private val usuarioRepository: UsuarioRepository,
    private val refreshTokenRepository: RefreshTokenRepository
) : UsernamePasswordAuthenticationFilter() {

    companion object {
        const val ATTR_USERNAME = "login.username"
    }

    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse?): Authentication? {
        val authCredentials = try {
            ObjectMapper().readValue(request.reader, AuthCredentials::class.java)
        } catch (e: Exception) {
            throw AuthenticationServiceException("Error en parseo de las credenciales de acceso", e)
        }

        if (authCredentials?.username.isNullOrBlank() || authCredentials.password.isNullOrBlank()) {
            throw AuthenticationServiceException("Username o password no provistos")
        }

        // Control de fuerza bruta: se corta antes de tocar la base o BCrypt
        if (loginAttemptService.estaBloqueado(request.remoteAddr, authCredentials.username)) {
            throw LockedException("Demasiados intentos de login")
        }

        // Lo usa el failure handler para contar el fallo contra este usuario
        request.setAttribute(ATTR_USERNAME, authCredentials.username)

        val usernamePAT = UsernamePasswordAuthenticationToken(
            authCredentials.username,
            authCredentials.password,
            emptyList()
        )

        return authenticationManager.authenticate(usernamePAT)
    }

    override fun successfulAuthentication(request: HttpServletRequest?, response: HttpServletResponse?, chain: FilterChain?, authResult: Authentication?) {
        val userDetails : UserDetailImpl = authResult?.principal as UserDetailImpl

        loginAttemptService.registrarExito(userDetails.username)
        userDetails.username?.let { usuarioRepository.actualizarUltimoLogin(it, LocalDateTime.now()) }

        val token : String = TokenUtils.createToken(userDetails.getNombre(), userDetails.username)
        val refreshToken = RefreshToken(
            username = requireNotNull(userDetails.username) { "El usuario autenticado no tiene username" },
            expiryDate = Instant.now().plus(30, ChronoUnit.DAYS)
        )
        refreshTokenRepository.save(refreshToken)

        if (response != null) {
            response.addHeader("Authorization", "Bearer $token")
            response.addHeader("X-Refresh-Token", refreshToken.token)
            response.writer.flush()
        }

        super.successfulAuthentication(request, response, chain, authResult)
    }

}
