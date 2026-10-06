package com.estonianport.agendaza.service

import com.estonianport.agendaza.dto.GastoDTO
import com.estonianport.agendaza.dto.TotalesPagosMes
import com.estonianport.agendaza.errors.BusinessException
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Evento
import com.estonianport.agendaza.model.Gasto
import com.estonianport.agendaza.model.Salon
import com.estonianport.agendaza.model.TipoEvento
import com.estonianport.agendaza.model.Usuario
import com.estonianport.agendaza.model.enums.Duracion
import com.estonianport.agendaza.model.enums.Estado
import com.estonianport.agendaza.model.enums.TipoGasto
import com.estonianport.agendaza.repository.GastoRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Optional

class GastoServiceTest {

    private val repository = org.mockito.kotlin.mock<GastoRepository>()
    private val eventoService = org.mockito.kotlin.mock<EventoService>()
    private val usuarioService = org.mockito.kotlin.mock<UsuarioService>()
    private val empresaService = org.mockito.kotlin.mock<EmpresaService>()
    private lateinit var service: GastoService

    private val empresa: Empresa = Salon(1L, "Salon", 123L, "salon@test.com", "Calle", 1, "Ciudad")
    private val usuario = Usuario(2L, "Ana", "Perez", 456L, "ana@test.com")

    @BeforeEach
    fun setUp() {
        service = GastoService(repository, eventoService, usuarioService, empresaService)
    }

    private fun buildGasto(
        id: Long = 5L,
        evento: Evento? = null,
        fecha: LocalDateTime = LocalDateTime.of(2025, 4, 10, 12, 0)
    ) = Gasto(
        id = id,
        monto = 125.50,
        tipoGasto = TipoGasto.MANTENIMIENTO,
        descripcion = "Reparación",
        fecha = fecha,
        evento = evento,
        empresa = empresa,
        encargado = usuario
    )

    private fun buildDTO(
        id: Long = 0L,
        eventoId: Long? = null,
        fecha: LocalDateTime = LocalDateTime.of(2025, 4, 10, 12, 0)
    ) = GastoDTO(id, 125.50, TipoGasto.MANTENIMIENTO, "Reparación", fecha, eventoId, 1L, 2L)

    private fun buildEvento(): Evento {
        val tipoEvento = TipoEvento(3L, "Fiesta", Duracion.MEDIO, 50, 10, LocalTime.of(4, 0), empresa)
        return Evento(
            id = 7L,
            nombre = "Evento",
            tipoEvento = tipoEvento,
            inicio = LocalDateTime.of(2025, 4, 20, 18, 0),
            fin = LocalDateTime.of(2025, 4, 20, 23, 0),
            capacidadAdultos = 50,
            capacidadNinos = 10,
            extraOtro = 0.0,
            descuento = 0L,
            listaExtra = mutableSetOf(),
            cateringOtro = 0.0,
            cateringOtroDescripcion = "",
            encargado = usuario,
            cliente = usuario,
            codigo = "ABCD",
            estado = Estado.RESERVADO,
            anotaciones = "",
            empresa = empresa
        )
    }

    @Test
    fun `getGastoDTO mapea el gasto encontrado`() {
        whenever(repository.findById(5L)).thenReturn(Optional.of(buildGasto()))

        val dto = service.getGastoDTO(5L)

        assertEquals(5L, dto.id)
        assertEquals(125.50, dto.monto)
        assertEquals(TipoGasto.MANTENIMIENTO, dto.tipoGasto)
        assertEquals("Reparación", dto.descripcion)
        assertNull(dto.eventoId)
        assertEquals(1L, dto.empresaId)
        assertEquals(2L, dto.usuarioId)
    }

    @Test
    fun `getGastoDTO falla si el gasto no existe`() {
        whenever(repository.findById(99L)).thenReturn(Optional.empty())

        val error = assertThrows(NotFoundException::class.java) { service.getGastoDTO(99L) }

        assertEquals("Gasto no encontrado con id: 99", error.message)
    }

    @Test
    fun `getAllGastoByMes consulta el intervalo desde inicio de mes hasta el siguiente`() {
        val desde = LocalDate.of(2025, 12, 1).atStartOfDay()
        val hasta = LocalDate.of(2026, 1, 1).atStartOfDay()
        val gastos = listOf(buildDTO())
        whenever(repository.getAllGastoByRango(1L, desde, hasta)).thenReturn(gastos)

        assertEquals(gastos, service.getAllGastoByMes(1L, 12, 2025))
        verify(repository).getAllGastoByRango(1L, desde, hasta)
    }

    @Test
    fun `totalesByRango devuelve los totales y consulta los limites mensuales`() {
        val desde = LocalDate.of(2026, 2, 1).atStartOfDay()
        val hasta = LocalDate.of(2026, 3, 1).atStartOfDay()
        val totales = TotalesPagosMes(900.0, 4L)
        whenever(repository.totalesByRango(1L, desde, hasta)).thenReturn(totales)

        assertEquals(totales, service.totalesByRango(1L, 2, 2026))
        verify(repository).totalesByRango(1L, desde, hasta)
    }

    @Test
    fun `las consultas mensuales rechazan meses fuera de rango`() {
        assertThrows(BusinessException::class.java) { service.getAllGastoByMes(1L, 0, 2026) }
        assertThrows(BusinessException::class.java) { service.totalesByRango(1L, 13, 2026) }
        verifyNoInteractions(repository)
    }

    @Test
    fun `saveGasto guarda un gasto sin evento y devuelve su DTO`() {
        val dto = buildDTO(fecha = LocalDateTime.of(2025, 4, 10, 12, 0))
        whenever(empresaService.get(1L)).thenReturn(empresa)
        whenever(usuarioService.get(2L)).thenReturn(usuario)
        whenever(repository.save(any<Gasto>())).thenAnswer { it.arguments[0] as Gasto }

        val guardado = service.saveGasto(dto)

        assertEquals(0L, guardado.id)
        assertNull(guardado.eventoId)
        assertEquals(dto.monto, guardado.monto)
        val captor = org.mockito.kotlin.argumentCaptor<Gasto>()
        verify(repository).save(captor.capture())
        assertNull(captor.firstValue.evento)
        assertEquals(empresa, captor.firstValue.empresa)
        assertEquals(usuario, captor.firstValue.encargado)
        verifyNoInteractions(eventoService)
    }

    @Test
    fun `saveGasto asocia un evento si el DTO trae su id`() {
        val evento = buildEvento()
        val dto = buildDTO(eventoId = evento.id)
        whenever(empresaService.get(1L)).thenReturn(empresa)
        whenever(usuarioService.get(2L)).thenReturn(usuario)
        whenever(eventoService.findById(evento.id)).thenReturn(evento)
        whenever(repository.save(any<Gasto>())).thenAnswer { it.arguments[0] as Gasto }

        val guardado = service.saveGasto(dto)

        assertEquals(evento.id, guardado.eventoId)
        val captor = org.mockito.kotlin.argumentCaptor<Gasto>()
        verify(repository).save(captor.capture())
        assertEquals(evento, captor.firstValue.evento)
        verify(eventoService).findById(evento.id)
    }

    @Test
    fun `saveGasto falla si no encuentra la empresa`() {
        whenever(empresaService.get(1L)).thenReturn(null)

        assertThrows(NotFoundException::class.java) { service.saveGasto(buildDTO()) }

        verifyNoInteractions(usuarioService, eventoService)
        verify(repository, never()).save(any<Gasto>())
    }

    @Test
    fun `saveGasto falla si no encuentra el encargado`() {
        whenever(empresaService.get(1L)).thenReturn(empresa)
        whenever(usuarioService.get(2L)).thenReturn(null)

        assertThrows(NotFoundException::class.java) { service.saveGasto(buildDTO()) }

        verifyNoInteractions(eventoService)
        verify(repository, never()).save(any<Gasto>())
    }

    @Test
    fun `delete marca la baja logica y persiste el gasto`() {
        val gasto = buildGasto()
        whenever(repository.findById(5L)).thenReturn(Optional.of(gasto))
        whenever(repository.save(gasto)).thenReturn(gasto)

        service.delete(5L)

        assertEquals(LocalDate.now(), gasto.fechaBaja)
        verify(repository).save(gasto)
    }

    @Test
    fun `delete lanza NotFoundException si el gasto no existe`() {
        whenever(repository.findById(99L)).thenReturn(Optional.empty())

        assertThrows(NotFoundException::class.java) { service.delete(99L) }

        verify(repository, never()).save(any<Gasto>())
    }
}
