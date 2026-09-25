package com.estonianport.agendaza.model

import com.estonianport.agendaza.model.enums.Duracion
import com.estonianport.agendaza.model.enums.TipoExtra
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class PrecioConFechaTest {

    private val empresa = Salon(1L, "Salon", 123L, "salon@test.com", "Calle", 1, "Ciudad")
    private val desde = LocalDateTime.of(2026, 9, 1, 0, 0)
    private val hasta = LocalDateTime.of(2026, 9, 30, 23, 59)

    @Test
    fun `PrecioConFechaExtra toDTO incluye id del extra y datos del precio`() {
        val extra = Extra(2L, "Mozo", TipoExtra.VARIABLE_EVENTO)
        val precio = PrecioConFechaExtra(3L, 500.0, desde, hasta, empresa, extra)

        val dto = precio.toDTO()

        assertEquals(3L, dto.id)
        assertEquals(desde, dto.desde)
        assertEquals(hasta, dto.hasta)
        assertEquals(500.0, dto.precio)
        assertEquals(empresa.id, dto.empresaId)
        assertEquals(extra.id, dto.itemId)
    }

    @Test
    fun `PrecioConFechaTipoEvento toDTO incluye id del tipo de evento y datos del precio`() {
        val tipoEvento = TipoEvento(
            4L, "Fiesta", Duracion.MEDIO,
            80, 10, java.time.LocalTime.of(5, 0), empresa
        )
        val precio = PrecioConFechaTipoEvento(5L, 900.0, desde, hasta, empresa, tipoEvento)

        val dto = precio.toDTO()

        assertEquals(5L, dto.id)
        assertEquals(desde, dto.desde)
        assertEquals(hasta, dto.hasta)
        assertEquals(900.0, dto.precio)
        assertEquals(empresa.id, dto.empresaId)
        assertEquals(tipoEvento.id, dto.itemId)
    }
}
