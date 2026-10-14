package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.EventoReservaDTO
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.Usuario
import com.estonianport.agendaza.model.enums.Estado
import com.estonianport.agendaza.service.EventoService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class EventoControllerTest {
    private val eventoService = mock<EventoService>()
    private val controller = EventoController(eventoService)

    @Test
    fun `getEvento lanza not found cuando el servicio no encuentra el evento`() {
        whenever(eventoService.getEventoVer(15L)).thenReturn(null)

        assertThrows(NotFoundException::class.java) { controller.getEvento(15L) }
    }

    @Test
    fun `saveEvento delega reserva y devuelve created con id`() {
        val reserva = EventoReservaDTO(
            id = 0L,
            nombre = "Fiesta",
            capacidadAdultos = 40,
            capacidadNinos = 5,
            codigo = "ABCD",
            inicio = LocalDateTime.of(2026, 11, 1, 18, 0),
            fin = LocalDateTime.of(2026, 11, 1, 23, 0),
            tipoEventoId = 2L,
            empresaId = 3L,
            extraOtro = 0.0,
            extraOtroDescripcion = "",
            listaExtra = emptyList(),
            cateringOtro = 0.0,
            cateringOtroDescripcion = "",
            cliente = Usuario(0L, "Ana", "Perez", 123L, "ana@test.com"),
            encargadoId = 4L,
            estado = Estado.RESERVADO,
            anotaciones = ""
        )
        whenever(eventoService.registrarReserva(any())).thenReturn(91L)

        val response = controller.saveEvento(reserva)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(91L, response.body?.data)
        verify(eventoService).registrarReserva(reserva)
    }
}
