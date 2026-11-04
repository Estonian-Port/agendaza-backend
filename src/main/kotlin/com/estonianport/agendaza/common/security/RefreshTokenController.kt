package com.estonianport.agendaza.common.security

import com.estonianport.agendaza.dto.response.CustomResponse
import com.estonianport.agendaza.service.UsuarioService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/auth")
@CrossOrigin("*")
class RefreshTokenController(
    private val refreshTokenService: RefreshTokenService,
    private val usuarioService: UsuarioService
) {

    @PostMapping("/refresh")
    fun refresh(@RequestBody request: RefreshDTO): ResponseEntity<CustomResponse<Any>> {
        val nuevo = refreshTokenService.rotar(request.refreshToken)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    CustomResponse(
                        message = "Refresh Token inválido o expirado",
                        data = request.refreshToken
                    )
                )

        val usuario = usuarioService.getByUsername(nuevo.username)
        if (usuario == null) {
            refreshTokenService.revocar(nuevo.token)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            CustomResponse(
                        message = "Usuario inactivo",
                        data = nuevo.username
                    )
                )
        }

        val accessToken = TokenUtils.createToken(usuario.nombre, usuario.username)
        return ResponseEntity.ok(
            CustomResponse<Any>(
                message = "Token renovado correctamente",
                data = mapOf("accessToken" to accessToken, "refreshToken" to nuevo.token)
            )
        )
    }

    @PostMapping("/logout")
    fun logout(@RequestBody request: RefreshDTO): ResponseEntity<Void> {
        refreshTokenService.revocar(request.refreshToken)
        return ResponseEntity.noContent().build()
    }
}
