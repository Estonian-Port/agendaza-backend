package com.estonianport.agendaza.service

import com.estonianport.agendaza.dto.UsuarioAbmDTO
import com.estonianport.agendaza.dto.UsuarioPerfilDTO
import com.estonianport.agendaza.dto.UsuarioResponseDto
import com.estonianport.agendaza.model.Usuario
import com.estonianport.agendaza.repository.UsuarioRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDate
import java.util.Optional

class UsuarioServiceTest {

    private val usuarioRepository = mock<UsuarioRepository>()
    private lateinit var service: UsuarioService

    @BeforeEach
    fun setUp() {
        service = UsuarioService(usuarioRepository)
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
            whenever(usuarioRepository.findAllByEmail("test@test.com")).thenReturn(listOf(usuario))
            assertEquals(usuario, service.getByEmail("test@test.com"))
        }

        @Test
        fun `devuelve null cuando no existe`() {
            whenever(usuarioRepository.findAllByEmail("noexiste@test.com")).thenReturn(emptyList())
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
        fun `lanza NotFoundException cuando no existe`() {
            whenever(usuarioRepository.findById(99L)).thenReturn(Optional.empty())

            val error = assertThrows(com.estonianport.agendaza.errors.NotFoundException::class.java) {
                service.findById(99L)
            }

            assertEquals("Usuario no encontrado", error.message)
        }
    }

    // ── getByCelular ──────────────────────────────────────────────────────────

    @Nested
    inner class GetByCelularTest {

        @Test
        fun `devuelve usuario por celular`() {
            val usuario = buildUsuario()
            whenever(usuarioRepository.findAllByCelular(1234567890L)).thenReturn(listOf(usuario))
            assertEquals(usuario, service.getByCelular(1234567890L))
        }

        @Test
        fun `devuelve null si no existe el celular`() {
            whenever(usuarioRepository.findAllByCelular(0L)).thenReturn(emptyList())
            assertNull(service.getByCelular(0L))
        }
    }

    @Test
    fun `getUsuarioDtoByEmail devuelve el DTO del repositorio`() {
        val esperado = UsuarioResponseDto(1L, "Juan", "Pérez", "juan", "test@test.com", 1234567890L)
        whenever(usuarioRepository.getUsuarioDtoByEmail("test@test.com")).thenReturn(esperado)

        assertEquals(esperado, service.getUsuarioDtoByEmail("test@test.com"))
    }

    @Test
    fun `getUsuarioDtoByEmail falla cuando no existe el usuario`() {
        whenever(usuarioRepository.getUsuarioDtoByEmail("missing@test.com")).thenReturn(null)

        assertThrows(com.estonianport.agendaza.errors.NotFoundException::class.java) {
            service.getUsuarioDtoByEmail("missing@test.com")
        }
    }

    @Test
    fun `getUsuarioDtoByCelular y por username devuelven los DTOs del repositorio`() {
        val porCelular = UsuarioResponseDto(1L, "Juan", "Pérez", "juan", "test@test.com", 1234567890L)
        val porUsername = UsuarioResponseDto(1L, "Juan", "Pérez", "juan", "test@test.com", 1234567890L)
        whenever(usuarioRepository.getUsuarioDtoByCelular(1234567890L)).thenReturn(porCelular)
        whenever(usuarioRepository.getUsuarioDtoByUsername("juan")).thenReturn(porUsername)

        assertEquals(porCelular, service.getUsuarioDtoByCelular(1234567890L))
        assertEquals(porUsername, service.getUsuarioDtoByUsername("juan"))
    }

    @Test
    fun `consultas de DTO por celular y username lanzan NotFoundException cuando faltan`() {
        whenever(usuarioRepository.getUsuarioDtoByCelular(0L)).thenReturn(null)
        whenever(usuarioRepository.getUsuarioDtoByUsername("missing")).thenReturn(null)

        assertThrows(com.estonianport.agendaza.errors.NotFoundException::class.java) {
            service.getUsuarioDtoByCelular(0L)
        }
        assertThrows(com.estonianport.agendaza.errors.NotFoundException::class.java) {
            service.getUsuarioDtoByUsername("missing")
        }
    }

    @Test
    fun `getUsuarioPerfil devuelve el perfil del repositorio`() {
        val perfil = UsuarioPerfilDTO(1L, "Juan", "Pérez", "juan", "test@test.com", 1234567890L, LocalDate.of(1990, 1, 2))
        whenever(usuarioRepository.getUsuarioPerfil(1L)).thenReturn(perfil)

        assertEquals(perfil, service.getUsuarioPerfil(1L))
    }

    @Test
    fun `getUsuarioPerfil lanza NotFoundException cuando no hay perfil`() {
        whenever(usuarioRepository.getUsuarioPerfil(99L)).thenReturn(null)

        assertThrows(com.estonianport.agendaza.errors.NotFoundException::class.java) {
            service.getUsuarioPerfil(99L)
        }
    }

    @Test
    fun `getUsuarioOfEmpresa devuelve el cargo del repositorio o falla si falta`() {
        val cargo = com.estonianport.agendaza.dto.UsuarioEditCargoDTO(2L, 5L, com.estonianport.agendaza.model.enums.TipoCargo.ENCARGADO)
        whenever(usuarioRepository.getUsuarioOfEmpresa(2L, 5L)).thenReturn(cargo)
        whenever(usuarioRepository.getUsuarioOfEmpresa(9L, 5L)).thenReturn(null)

        assertEquals(cargo, service.getUsuarioOfEmpresa(2L, 5L))
        assertThrows(com.estonianport.agendaza.errors.NotFoundException::class.java) {
            service.getUsuarioOfEmpresa(9L, 5L)
        }
    }

    @Test
    fun `getAllUsuario pagina y devuelve los usuarios de la empresa`() {
        val usuarios = listOf(UsuarioAbmDTO(1L, "Juan", "Pérez", "juan"))
        whenever(usuarioRepository.getAllUsuario(eq(5L), any())).thenReturn(PageImpl(usuarios))

        assertEquals(usuarios, service.getAllUsuario(5L, 2))
        verify(usuarioRepository).getAllUsuario(5L, PageRequest.of(2, 10))
    }

    @Test
    fun `getAllClienteFiltrados pasa empresa busqueda y pagina`() {
        val clientes = listOf(UsuarioAbmDTO(2L, "Ana", "López", null))
        whenever(usuarioRepository.getAllClienteFiltrados(eq(7L), eq("ana"), any()))
            .thenReturn(PageImpl(clientes))

        assertEquals(clientes, service.getAllClienteFiltrados(7L, 1, "ana"))
        verify(usuarioRepository).getAllClienteFiltrados(7L, "ana", PageRequest.of(1, 10))
    }

    @Test
    fun `listados paginados de empleados y clientes delegan a la pagina correcta`() {
        val pageable = PageRequest.of(3, 10)
        val empleados = listOf(UsuarioAbmDTO(1L, "Juan", "Pérez", "juan"))
        val clientes = listOf(UsuarioAbmDTO(2L, "Ana", "López", null))
        whenever(usuarioRepository.getAllUsuarioFiltrados(eq(4L), eq("juan"), any())).thenReturn(PageImpl(empleados))
        whenever(usuarioRepository.getAllCliente(eq(4L), any())).thenReturn(PageImpl(clientes))

        assertEquals(empleados, service.getAllUsuarioFiltrados(4L, 3, "juan"))
        assertEquals(clientes, service.getAllCliente(4L, 3))
        verify(usuarioRepository).getAllUsuarioFiltrados(4L, "juan", pageable)
        verify(usuarioRepository).getAllCliente(4L, pageable)
    }

    @Test
    fun `cantidades de empleados y clientes delegan al repositorio`() {
        whenever(usuarioRepository.getCantidadUsuario(4L)).thenReturn(7)
        whenever(usuarioRepository.getCantidadFiltrados(4L, "juan")).thenReturn(2)
        whenever(usuarioRepository.getCantidadCliente(4L)).thenReturn(15)
        whenever(usuarioRepository.getCantidadClienteFiltrados(4L, "ana")).thenReturn(3)

        assertEquals(7, service.getCantidadUsuario(4L))
        assertEquals(2, service.getCantidadFiltrados(4L, "juan"))
        assertEquals(15, service.getCantidadCliente(4L))
        assertEquals(3, service.getCantidadClienteFiltrados(4L, "ana"))
    }

    @Test
    fun `getAllEmpresaByUsuarioId y save delegan al repositorio`() {
        val empresa = com.estonianport.agendaza.dto.EmpresaAbmDTO(
            8L, "Salon", com.estonianport.agendaza.model.enums.TipoCargo.ENCARGADO,
            "salon@test.com", 123L, "Calle", 1, "Ciudad"
        )
        val usuario = buildUsuario()
        whenever(usuarioRepository.getAllEmpresaByUsuarioId(1L)).thenReturn(listOf(empresa))
        whenever(usuarioRepository.save(usuario)).thenReturn(usuario)

        assertEquals(listOf(empresa), service.getAllEmpresaByUsuarioId(1L))
        assertSame(usuario, service.save(usuario))
        verify(usuarioRepository).save(usuario)
    }
}
