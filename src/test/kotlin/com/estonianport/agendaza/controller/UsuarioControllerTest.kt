package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.UsuarioDTORequest
import com.estonianport.agendaza.model.Usuario
import com.estonianport.agendaza.service.CargoService
import com.estonianport.agendaza.service.EmpresaService
import com.estonianport.agendaza.service.UsuarioService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class UsuarioControllerTest {
    private val usuarioService = mock<UsuarioService>()
    private val empresaService = mock<EmpresaService>()
    private val cargoService = mock<CargoService>()
    private val passwordEncoder = BCryptPasswordEncoder()
    private val controller = UsuarioController(usuarioService, empresaService, cargoService, passwordEncoder)

    @Test
    fun `saveUsuario cifra password de usuario nuevo antes de guardar`() {
        val usuario = Usuario(0L, "Ana", "Perez", 123L, "ana@test.com").apply {
            username = "ana"
            password = "secreto"
        }
        whenever(usuarioService.save(any())).thenAnswer { invocation ->
            (invocation.arguments[0] as Usuario).apply { id = 21L }
        }

        val response = controller.saveUsuario(UsuarioDTORequest(usuario, 0L, null))

        val guardado = argumentCaptor<Usuario>()
        verify(usuarioService).save(guardado.capture())
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(21L, response.body?.data?.id)
        assertNotEquals("secreto", guardado.firstValue.password)
        assertTrue(passwordEncoder.matches("secreto", guardado.firstValue.password!!))
    }
}
