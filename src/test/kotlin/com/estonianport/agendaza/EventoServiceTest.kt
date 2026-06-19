package com.estonianport.agendaza

import com.estonianport.agendaza.common.emailService.EmailService
import com.estonianport.agendaza.common.openPDF.PdfService
import com.estonianport.agendaza.dto.EventoDTO
import com.estonianport.agendaza.dto.toEventoDto
import com.estonianport.agendaza.model.Capacidad
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Evento
import com.estonianport.agendaza.model.TipoEvento
import com.estonianport.agendaza.model.Usuario
import com.estonianport.agendaza.model.enums.Estado
import com.estonianport.agendaza.repository.EventoRepository
import com.estonianport.agendaza.service.CapacidadService
import com.estonianport.agendaza.service.EmpresaService
import com.estonianport.agendaza.service.EventoService
import com.estonianport.agendaza.service.ExtraService
import com.estonianport.agendaza.service.ExtraVariableService
import com.estonianport.agendaza.service.TipoEventoService
import com.estonianport.agendaza.service.UsuarioService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.time.LocalDateTime
import java.util.Optional

class EventoServiceTest {

    // ── Mocks ─────────────────────────────────────────────────────────────────

    private val eventoRepository   = mock<EventoRepository>()
    private val empresaService     = mock<EmpresaService>()
    private val extraService       = mock<ExtraService>()
    private val extraVariableService = mock<ExtraVariableService>()
    private val emailService       = mock<EmailService>()
    private val usuarioService     = mock<UsuarioService>()
    private val pdfService         = mock<PdfService>()
    private val tipoEventoService  = mock<TipoEventoService>()
    private val capacidadService   = mock<CapacidadService>()

    private lateinit var service: EventoService

    @BeforeEach
    fun setUp() {
        service = EventoService(
            eventoRepository, empresaService, extraService, extraVariableService,
            emailService, usuarioService, pdfService, tipoEventoService, capacidadService
        )
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun buildEvento(id: Long = 1L, nombre: String = "evento test"): Evento {
        val empresa    = mock<Empresa>()
        val tipoEvento = mock<TipoEvento>().also { whenever(it.id).thenReturn(1L) }
        val encargado  = mock<Usuario>()
        val cliente    = mock<Usuario>().also {
            whenever(it.email).thenReturn("cliente@test.com")
        }
        val capacidad  = Capacidad(1L, 100, 20)
        return Evento(
            id = id, nombre = nombre, tipoEvento = tipoEvento,
            inicio = LocalDateTime.now(), fin = LocalDateTime.now().plusHours(5),
            capacidad = capacidad, extraOtro = 0.0, descuento = 0L,
            listaExtra = mutableSetOf(), cateringOtro = 0.0,
            cateringOtroDescripcion = "", encargado = encargado, cliente = cliente,
            codigo = "ABCD", estado = Estado.RESERVADO, anotaciones = "", empresa = empresa
        )
    }

    private fun buildEventoDTO(id: Long = 1L, nombre: String = "evento test"): EventoDTO {
        return EventoDTO(
            id = id,
            nombre = nombre,
            codigo = "ABCD",
            inicio = LocalDateTime.now(),
            fin = LocalDateTime.now().plusHours(5),
            tipoEvento = "Casamiento"
        )
    }

    // ── findById ─────────────────────────────────────────────────────────────

    @Nested
    inner class FindByIdTest {

        @Test
        fun `findById devuelve evento cuando existe`() {
            val evento = buildEvento()
            whenever(eventoRepository.findById(1L)).thenReturn(Optional.of(evento))
            assertEquals(evento, service.findById(1L))
        }

        @Test
        fun `findById lanza excepcion cuando no existe`() {
            whenever(eventoRepository.findById(99L)).thenReturn(Optional.empty())
            assertThrows(IllegalArgumentException::class.java) { service.findById(99L) }
        }
    }

    // ── getHorarioDisponible ──────────────────────────────────────────────────

    @Nested
    inner class HorarioDisponibleTest {

        private val desde = LocalDateTime.of(2025, 10, 1, 18, 0)
        private val hasta = LocalDateTime.of(2025, 10, 1, 23, 0)

        @Test
        fun `devuelve true cuando el horario esta libre`() {
            whenever(eventoRepository.existeSuperposicionDeHorarios(1L, desde, hasta)).thenReturn(false)
            assertTrue(service.getHorarioDisponible(1L, desde, hasta))
        }

        @Test
        fun `devuelve false cuando hay superposicion`() {
            whenever(eventoRepository.existeSuperposicionDeHorarios(1L, desde, hasta)).thenReturn(true)
            assertFalse(service.getHorarioDisponible(1L, desde, hasta))
        }
    }

    // ── getEventosOcupadosDelDia ──────────────────────────────────────────────

    @Nested
    inner class EventosOcupadosDelDiaTest {

        @Test
        fun `devuelve lista vacia cuando no hay eventos ese dia`() {
            whenever(eventoRepository.findAllByInicioBetweenAndEmpresa(any(), any(), any()))
                .thenReturn(emptyList())

            val result = service.getEventosOcupadosDelDia(1L, LocalDateTime.now())
            assertTrue(result.isEmpty())
        }

        @Test
        fun `formatea correctamente un evento que inicia y termina el mismo dia`() {
            val inicio = LocalDateTime.of(2025, 9, 10, 18, 0)
            val fin    = LocalDateTime.of(2025, 9, 10, 23, 0)
            val evento = buildEventoDTO(nombre = "boda").also {
                it.inicio = inicio
                it.fin    = fin
            }

            whenever(eventoRepository.findAllByInicioBetweenAndEmpresa(any(), any(), any()))
                .thenReturn(listOf(evento))

            val result = service.getEventosOcupadosDelDia(1L, inicio)
            assertEquals(1, result.size)
            assertTrue(result[0].contains("18:00"))
            assertTrue(result[0].contains("23:00"))
            assertFalse(result[0].contains("del dia"))   // mismo día → no agrega texto extra
        }

        @Test
        fun `formatea correctamente un evento que termina al dia siguiente`() {
            val inicio = LocalDateTime.of(2025, 9, 10, 22, 0)
            val fin    = LocalDateTime.of(2025, 9, 11,  3, 0)
            val evento = buildEventoDTO(nombre = "after").also {
                it.inicio = inicio
                it.fin    = fin
            }

            whenever(eventoRepository.findAllByInicioBetweenAndEmpresa(any(), any(), any()))
                .thenReturn(listOf(evento))

            val result = service.getEventosOcupadosDelDia(1L, inicio)
            assertTrue(result[0].contains("del dia"))
            assertTrue(result[0].contains("2025-09-11"))
        }
    }

    // ── getAllEstado ──────────────────────────────────────────────────────────

    @Nested
    inner class AllEstadoTest {

        @Test
        fun `getAllEstado devuelve todos los estados`() {
            val estados = service.getAllEstado()
            assertTrue(estados.contains("RESERVADO"))
            assertTrue(estados.contains("COTIZADO"))
        }

        @Test
        fun `getAllEstadoForSaveEvento devuelve solo COTIZADO y RESERVADO`() {
            val estados = service.getAllEstadoForSaveEvento()
            assertEquals(2, estados.size)
            assertTrue(estados.contains("COTIZADO"))
            assertTrue(estados.contains("RESERVADO"))
        }
    }

    // ── delete (soft delete) ──────────────────────────────────────────────────

    @Nested
    inner class DeleteTest {

        @Test
        fun `delete hace soft-delete seteando fechaBaja`() {
            val evento = buildEvento()
            whenever(eventoRepository.findById(1L)).thenReturn(Optional.of(evento))
            whenever(eventoRepository.save(any<Evento>())).thenAnswer { it.arguments[0] as Evento }

            service.delete(1L)

            assertNotNull(evento.fechaBaja)
            verify(eventoRepository).save(evento)
        }

        @Test
        fun `delete lanza excepcion si el evento no existe`() {
            whenever(eventoRepository.findById(99L)).thenReturn(Optional.empty())
            assertThrows(IllegalArgumentException::class.java) { service.delete(99L) }
        }
    }
}