package com.estonianport.agendaza.service

import com.estonianport.agendaza.common.emailService.EmailService
import com.estonianport.agendaza.common.openPDF.PdfService
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.errors.BusinessException
import com.estonianport.agendaza.dto.TotalesPagosMes
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional

class PagoServiceTest {

    private val pagoRepository  = mock<PagoRepository>()
    private val gastoService    = mock<GastoService>()
    private val eventoService   = mock<EventoService>()
    private val usuarioService  = mock<UsuarioService>()
    private val empresaService  = mock<EmpresaService>()
    private val pdfService      = mock<PdfService>()
    private val emailService    = mock<EmailService>()

    private lateinit var service: PagoService

    @BeforeEach
    fun setUp() {
        service = PagoService(
            pagoRepository, gastoService, eventoService, usuarioService,
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

    private fun buildEventoForPago(): Evento {
        val evento = mock<Evento>()
        val empresa = mock<Empresa>()
        val encargado = Usuario(1L, "Ana", "Perez", 123L, "ana@test.com")
        whenever(evento.codigo).thenReturn("ABCD")
        whenever(evento.nombre).thenReturn("Boda")
        whenever(evento.inicio).thenReturn(LocalDateTime.of(2025, 1, 1, 18, 0))
        whenever(evento.empresa).thenReturn(empresa)
        whenever(empresa.id).thenReturn(1L)
        whenever(evento.encargado).thenReturn(encargado)
        return evento
    }

    private fun buildPagoEntity(id: Long = 5L): Pago = Pago(
        id = id,
        monto = 1000.0,
        concepto = Concepto.SENIA,
        medioDePago = MedioDePago.EFECTIVO,
        fecha = LocalDateTime.of(2025, 1, 2, 12, 0),
        evento = buildEventoForPago(),
        encargado = Usuario(1L, "Ana", "Perez", 123L, "ana@test.com")
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
            val pago = buildPagoEntity()
            whenever(pagoRepository.findById(5L)).thenReturn(Optional.of(pago))

            val dto = service.getPagoDTO(5L)
            assertEquals(5L, dto.id)
            assertEquals(1000.0, dto.monto)
            assertEquals("ABCD", dto.codigo)
            assertEquals(1L, dto.empresaId)
            assertEquals(1L, dto.usuarioId)
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
            val evento   = buildEventoForPago()
            val encargado = mock<Usuario>()

            whenever(eventoService.getByCodigoAndEmpresaId("ABCD", 1L)).thenReturn(evento)
            whenever(usuarioService.get(1L)).thenReturn(encargado)
            whenever(pagoRepository.save(any<Pago>())).thenAnswer { it.arguments[0] as Pago }

            val result = service.savePago(dto)
            assertEquals(0L, result.id)
            assertEquals("ABCD", result.codigo)
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
    fun `getAllPagoFromEvento devuelve los pagos del evento`() {
        val pagos = listOf(buildPagoDTO(id = 2L))
        whenever(pagoRepository.getAllPagoFromEvento(4L)).thenReturn(pagos)

        assertEquals(pagos, service.getAllPagoFromEvento(4L))
        verify(pagoRepository).getAllPagoFromEvento(4L)
    }

    @Test
    fun `getAllPagoFromEvento falla cuando el repositorio no encuentra pagos`() {
        whenever(pagoRepository.getAllPagoFromEvento(4L)).thenReturn(null)

        assertThrows(NotFoundException::class.java) { service.getAllPagoFromEvento(4L) }
    }

    @Test
    fun `getEventoForEditEventoPago actualiza el precio total con el presupuesto del evento`() {
        val evento = mock<Evento>()
        val eventoPago = com.estonianport.agendaza.dto.EventoPagoDTO(4L, "Boda", "ABCD", 0.0)
        whenever(eventoService.findById(4L)).thenReturn(evento)
        whenever(pagoRepository.getEventoForPago(4L)).thenReturn(eventoPago)
        whenever(evento.getPresupuestoTotal()).thenReturn(2500.0)

        assertEquals(eventoPago, service.getEventoForEditEventoPago(4L))
        assertEquals(2500.0, eventoPago.precioTotal)
    }

    @Test
    fun `getEventoForEditEventoPago falla si el repositorio no encuentra el evento`() {
        whenever(eventoService.findById(4L)).thenReturn(mock())
        whenever(pagoRepository.getEventoForPago(4L)).thenReturn(null)

        assertThrows(NotFoundException::class.java) { service.getEventoForEditEventoPago(4L) }
    }

    @Test
    fun `getAllPagoByMes consulta desde el primer dia hasta el primer dia del mes siguiente`() {
        val pagos = listOf(buildPagoDTO(id = 7L))
        val desde = LocalDate.of(2025, 2, 1).atStartOfDay()
        val hasta = LocalDate.of(2025, 3, 1).atStartOfDay()
        whenever(pagoRepository.getAllPagoByRango(1L, desde, hasta)).thenReturn(pagos)

        assertEquals(pagos, service.getAllPagoByMes(1L, 2, 2025))
        verify(pagoRepository).getAllPagoByRango(1L, desde, hasta)
    }

    @Test
    fun `getAllPagoByMes rechaza un mes fuera de rango`() {
        assertThrows(BusinessException::class.java) { service.getAllPagoByMes(1L, 13, 2025) }
        verifyNoInteractions(pagoRepository)
    }

    @Test
    fun `getResumenPagosMes calcula ingresos balance cantidad y total del rango`() {
        val desde = LocalDate.of(2025, 12, 1).atStartOfDay()
        val hasta = LocalDate.of(2026, 1, 1).atStartOfDay()
        whenever(pagoRepository.totalesByRango(1L, desde, hasta)).thenReturn(TotalesPagosMes(3200.0, 3L))
        whenever(gastoService.totalesByRango(1L, 12, 2025)).thenReturn(TotalesPagosMes(800.0, 2L))

        val resumen = service.getResumenPagosMes(1L, 12, 2025)

        assertEquals(3200.0, resumen.ingresos)
        assertEquals(800.0, resumen.egresos)
        assertEquals(2400.0, resumen.balance)
        assertEquals(3L, resumen.cantidadIngresos)
        assertEquals(2L, resumen.cantidadEgresos)
        verify(pagoRepository).totalesByRango(1L, desde, hasta)
        verify(gastoService).totalesByRango(1L, 12, 2025)
    }

    @Test
    fun `generarComprobantePago entrega el PDF generado`() {
        val pago = mock<Pago>()
        val pdf = byteArrayOf(1, 2, 3)
        whenever(pagoRepository.findById(5L)).thenReturn(Optional.of(pago))
        whenever(pdfService.generarComprobanteDePago(pago)).thenReturn(pdf)

        assertArrayEquals(pdf, service.generarComprobantePago(5L))
        verify(pdfService).generarComprobanteDePago(pago)
    }

    @Test
    fun `generarEstadoCuenta delega en PDF con el evento encontrado`() {
        val evento = mock<Evento>()
        val pdf = byteArrayOf(4, 5)
        whenever(eventoService.findById(6L)).thenReturn(evento)
        whenever(pdfService.generarEstadoDeCuenta(evento)).thenReturn(pdf)

        assertArrayEquals(pdf, service.generarEstadoCuenta(6L))
        verify(pdfService).generarEstadoDeCuenta(evento)
    }

    @Test
    fun `enviarEmailPago falla si no existe la empresa`() {
        whenever(pagoRepository.findById(5L)).thenReturn(Optional.of(mock()))
        whenever(eventoService.findById(6L)).thenReturn(mock())
        whenever(empresaService.get(1L)).thenReturn(null)

        assertThrows(NotFoundException::class.java) { service.enviarEmailPago(5L, 6L, 1L) }
        verifyNoInteractions(emailService)
    }

    @Test
    fun `enviarEmailEstadoCuenta envia el estado de cuenta y devuelve true`() {
        val evento = mock<Evento>()
        val empresa: Empresa = Salon(1L, "Salon", 123L, "salon@test.com", "Calle", 1, "Ciudad")
        whenever(eventoService.findById(6L)).thenReturn(evento)
        whenever(empresaService.get(1L)).thenReturn(empresa)

        assertTrue(service.enviarEmailEstadoCuenta(6L, 1L))
        verify(emailService).enviarEmailEstadoCuenta(evento, empresa)
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
