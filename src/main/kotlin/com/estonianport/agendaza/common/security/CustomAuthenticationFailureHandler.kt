package com.estonianport.agendaza.common.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler

class CustomAuthenticationFailureHandler(
    private val loginAttemptService: LoginAttemptService
) : AuthenticationFailureHandler {

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        response.contentType = "application/json;charset=UTF-8"

        // Bloqueado: no se cuenta como un fallo más (si no, el bloqueo se renovaría solo)
        if (exception is LockedException) {
            response.status = 429
            response.setHeader("Retry-After", LoginAttemptService.VENTANA.seconds.toString())
            response.writer.write("{\"error\": \"Demasiados intentos. Probá de nuevo en unos minutos.\"}")
            return
        }

        loginAttemptService.registrarFallo(
            request.remoteAddr,
            request.getAttribute(JWTAuthenticationFilter.ATTR_USERNAME) as? String
        )

        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.writer.write("{\"error\": \"Usuario o password incorrectos\"}")
    }
}