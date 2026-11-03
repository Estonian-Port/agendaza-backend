package com.estonianport.agendaza.common.security

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/v1/auth")
@CrossOrigin("*")
class RefreshTokenController(
    private val refreshTokenRepository: RefreshTokenRepository,
    ){

    @PostMapping("/refresh")
    fun refreshToken(@RequestBody request: RefreshDTO): ResponseEntity<Any> {
        val storedToken = refreshTokenRepository.findByToken(request.refreshToken)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh Token inválido")

        if (storedToken.expiryDate.isBefore(Instant.now())) {
            refreshTokenRepository.delete(storedToken)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh Token expirado")
        }

        // Generar un nuevo Access Token
        val newAccessToken = TokenUtils.createToken(storedToken.username, storedToken.username)

        return ResponseEntity.ok(mapOf("accessToken" to newAccessToken, "refreshToken" to storedToken.token))
    }
}