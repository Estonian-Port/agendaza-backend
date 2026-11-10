package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.UsuarioDTORequest
import com.estonianport.agendaza.common.aceptacionTerminos.OnboardingService
import com.estonianport.agendaza.model.Usuario
import com.estonianport.agendaza.service.UsuarioService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus

class UsuarioControllerTest {
    private val usuarioService = mock<UsuarioService>()
    private val controller = UsuarioController(usuarioService, mock<OnboardingService>())

    @Test
    fun `saveUsuario delega la persistencia al servicio`() {
        val usuario = Usuario(0L, "Ana", "Perez", 123L, "ana@test.com").apply {
            username = "ana"
            password = "secreto"
        }
        usuario.id = 21L
        whenever(usuarioService.saveUsuario(any())).thenReturn(usuario)

        val response = controller.saveUsuario(UsuarioDTORequest(usuario, 0L, null))

        verify(usuarioService).saveUsuario(any())
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(21L, response.body?.data?.id)
    }
}
