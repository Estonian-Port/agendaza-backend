package com.estonianport.agendaza.service

import com.estonianport.agendaza.common.emailService.EmailService
import com.estonianport.agendaza.common.openPDF.PdfService
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.Evento
import com.estonianport.agendaza.model.Pago
import com.estonianport.agendaza.model.Usuario
import com.estonianport.agendaza.model.enums.Concepto
import com.estonianport.agendaza.model.enums.MedioDePago
import com.estonianport.agendaza.dto.PagoDTO
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Salon
import com.estonianport.agendaza.repository.PagoRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional

class PagoServiceTest {

    private val pagoRepository  = mock<PagoRepository>()
    private val eventoService   = mock<EventoService>()
    private val usuarioService  = mock<UsuarioService>()
    private val empresaService  = mock<EmpresaService>()
    private val pdfService      = mock<PdfService>()
    private val emailService    = mock<EmailService>()

    private lateinit var service: PagoService

    @BeforeEach
    fun setUp() {
        service = PagoService(
            pagoRepository, eventoService, usuarioService,
            empresaService, pdfService, emailService
        )
    }

    private fun buildPagoDTO(
        id: Long = 0L,
        monto: Double = 1000.0,
        fecha: LocalDateTime = LocalDateTime.now()
    ) = PagoDTO(
        id = id,
        monto = monto,
        codigo = "ABCD",
        medioDePago = MedioDePago.EFECTIVO,
        fechaEvento = LocalDateTime.now(),
        nombreEvento = "Boda",
        concepto = Concepto.SENIA,
        numeroCuota = null,
        empresaId = 1L,
        usuarioId = 1L,
        fecha = fecha
    )

    // ── getPagoDTO ────────────────────────────────────────────────────────────

    @Nested
    inner class GetPagoDTOTest {

        @Test
        fun `lanza NotFoundException si el pago no existe`() {
            whenever(pagoRepository.findById(99L)).thenReturn(Optional.empty())
            assertThrows(NotFoundException::class.java) { service.getPagoDTO(99L) }
        }

        @Test
        fun `devuelve PagoDTO correctamente`() {
            val pago = mock<Pago>()
            val dto  = buildPagoDTO(id = 5L)
            whenever(pagoRepository.findById(5L)).thenReturn(Optional.of(pago))
            whenever(pago.toDTO()).thenReturn(dto)

            assertEquals(dto, service.getPagoDTO(5L))
        }
    }

    // ── delete (soft-delete) ──────────────────────────────────────────────────

    @Nested
    inner class DeleteTest {

        @Test
        fun `delete setea fechaBaja en el pago`() {
            val pago = mock<Pago>()
            whenever(pagoRepository.findById(1L)).thenReturn(Optional.of(pago))
            whenever(pagoRepository.save(pago)).thenReturn(pago)

            service.delete(1L)

            verify(pago).fechaBaja = any<LocalDate>()
            verify(pagoRepository).save(pago)
        }

        @Test
        fun `delete lanza NotFoundException si el pago no existe`() {
            whenever(pagoRepository.findById(99L)).thenReturn(Optional.empty())
            assertThrows(NotFoundException::class.java) { service.delete(99L) }
        }
    }

    // ── contadorDePagos ───────────────────────────────────────────────────────

    @Nested
    inner class ContadorDePagosTest {

        @Test
        fun `devuelve la cantidad de pagos`() {
            whenever(pagoRepository.cantidadPagos(1L)).thenReturn(7)
            assertEquals(7, service.contadorDePagos(1L))
        }
    }

    // ── savePago ──────────────────────────────────────────────────────────────

    @Nested
    inner class SavePagoTest {

        @Test
        fun `lanza excepcion cuando el usuario no existe`() {
            val dto = buildPagoDTO()
            val evento = mock<Evento>()
            whenever(eventoService.getByCodigoAndEmpresaId("ABCD", 1L)).thenReturn(evento)
            whenever(usuarioService.get(1L)).thenReturn(null)

            assertThrows(NotFoundException::class.java) { service.savePago(dto) }
        }

        @Test
        fun `savePago guarda el pago y devuelve el DTO`() {
            val dto = buildPagoDTO(fecha = LocalDateTime.now().minusDays(1)) // fecha pasada → no usa now()
            val evento   = mock<Evento>()
            val encargado = mock<Usuario>()
            val savedPago = mock<Pago>()
            val savedDto  = buildPagoDTO(id = 10L)

            whenever(eventoService.getByCodigoAndEmpresaId("ABCD", 1L)).thenReturn(evento)
            whenever(usuarioService.get(1L)).thenReturn(encargado)
            whenever(pagoRepository.save(any<Pago>())).thenReturn(savedPago)
            whenever(savedPago.toDTO()).thenReturn(savedDto)

            val result = service.savePago(dto)
            assertEquals(10L, result.id)
            verify(pagoRepository).save(any<Pago>())
        }
    }

    @Test
    fun `getEventoForSavePago devuelve los datos preparados por el repositorio`() {
        val esperado = buildPagoDTO()
        whenever(pagoRepository.getEventoForSavePago(eq(4L), any())).thenReturn(esperado)

        assertEquals(esperado, service.getEventoForSavePago(4L))
        verify(pagoRepository).getEventoForSavePago(eq(4L), any())
    }

    @Test
    fun `getEventoForSavePago lanza NotFoundException cuando el evento no existe`() {
        whenever(pagoRepository.getEventoForSavePago(eq(99L), any())).thenReturn(null)

        assertThrows(NotFoundException::class.java) { service.getEventoForSavePago(99L) }
    }

    @Test
    fun `pagos pagina resultados y convierte cada pago a DTO`() {
        val pago = mock<Pago>()
        val dto = buildPagoDTO(id = 8L)
        whenever(pagoRepository.findAll(eq(2L), any())).thenReturn(PageImpl(listOf(pago)))
        whenever(pago.toDTO()).thenReturn(dto)

        assertEquals(listOf(dto), service.pagos(2L, 1))
        verify(pagoRepository).findAll(2L, PageRequest.of(1, 10))
    }

    @Test
    fun `enviarEmailPago envia el pago y devuelve true`() {
        val pago = mock<Pago>()
        val evento = mock<Evento>()
        val empresa: Empresa = Salon(1L, "Salon", 123L, "salon@test.com", "Calle", 1, "Ciudad")
        whenever(pagoRepository.findById(5L)).thenReturn(Optional.of(pago))
        whenever(eventoService.findById(6L)).thenReturn(evento)
        whenever(empresaService.get(1L)).thenReturn(empresa)

        assertTrue(service.enviarEmailPago(5L, 6L, 1L))
        verify(emailService).enviarEmailPago(pago, evento, empresa)
    }
}
