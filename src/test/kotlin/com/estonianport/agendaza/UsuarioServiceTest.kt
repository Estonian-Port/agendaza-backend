package com.estonianport.agendaza

import com.estonianport.agendaza.model.Usuario
import com.estonianport.agendaza.repository.UsuarioRepository
import com.estonianport.agendaza.service.UsuarioService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.util.Optional

class UsuarioServiceTest {

    private val usuarioRepository = mock<UsuarioRepository>()
    private lateinit var service: UsuarioService

    @BeforeEach
    fun setUp() {
        service = UsuarioService().also {
            it.usuarioRepository = usuarioRepository
        }
    }

    private fun buildUsuario(id: Long = 1L, email: String = "test@test.com") =
        Usuario(id, "Juan", "Pérez", 1234567890L, email)

    // ── existsByEmail ─────────────────────────────────────────────────────────

    @Nested
    inner class ExistsByEmailTest {

        @Test
        fun `devuelve true si el email existe`() {
            whenever(usuarioRepository.existsByEmail("test@test.com")).thenReturn(true)
            assertTrue(service.existsByEmail("test@test.com"))
        }

        @Test
        fun `devuelve false si el email no existe`() {
            whenever(usuarioRepository.existsByEmail("nuevo@test.com")).thenReturn(false)
            assertFalse(service.existsByEmail("nuevo@test.com"))
        }
    }

    // ── existsByCelular ───────────────────────────────────────────────────────

    @Nested
    inner class ExistsByCelularTest {

        @Test
        fun `devuelve true si el celular existe`() {
            whenever(usuarioRepository.existsByCelular(1111111111L)).thenReturn(true)
            assertTrue(service.existsByCelular(1111111111L))
        }

        @Test
        fun `devuelve false si el celular no existe`() {
            whenever(usuarioRepository.existsByCelular(9999999999L)).thenReturn(false)
            assertFalse(service.existsByCelular(9999999999L))
        }
    }

    // ── getByEmail ────────────────────────────────────────────────────────────

    @Nested
    inner class GetByEmailTest {

        @Test
        fun `devuelve usuario cuando existe`() {
            val usuario = buildUsuario()
            whenever(usuarioRepository.getByEmail("test@test.com")).thenReturn(usuario)
            assertEquals(usuario, service.getByEmail("test@test.com"))
        }

        @Test
        fun `devuelve null cuando no existe`() {
            whenever(usuarioRepository.getByEmail("noexiste@test.com")).thenReturn(null)
            assertNull(service.getByEmail("noexiste@test.com"))
        }
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Nested
    inner class FindByIdTest {

        @Test
        fun `devuelve usuario cuando existe`() {
            val usuario = buildUsuario()
            whenever(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario))
            assertEquals(usuario, service.findById(1L))
        }

        @Test
        fun `devuelve null cuando no existe`() {
            whenever(usuarioRepository.findById(99L)).thenReturn(Optional.empty())
            assertNull(service.findById(99L))
        }
    }

    // ── getByCelular ──────────────────────────────────────────────────────────

    @Nested
    inner class GetByCelularTest {

        @Test
        fun `devuelve usuario por celular`() {
            val usuario = buildUsuario()
            whenever(usuarioRepository.getByCelular(1234567890L)).thenReturn(usuario)
            assertEquals(usuario, service.getByCelular(1234567890L))
        }

        @Test
        fun `devuelve null si no existe el celular`() {
            whenever(usuarioRepository.getByCelular(0L)).thenReturn(null)
            assertNull(service.getByCelular(0L))
        }
    }
}