package com.estonianport.agendaza.model

import com.estonianport.agendaza.model.enums.TipoExtra
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ExtraTest {

    @Test
    fun `toDTO copia id nombre y tipo`() {
        val extra = Extra(2L, "Mozo", TipoExtra.VARIABLE_EVENTO)

        val dto = extra.toDTO()

        assertEquals(2L, dto.id)
        assertEquals("Mozo", dto.nombre)
        assertEquals(TipoExtra.VARIABLE_EVENTO, dto.tipoExtra)
    }

    @Test
    fun `toExtraPrecioDTO agrega el precio vigente de la empresa`() {
        val fecha = LocalDateTime.of(2026, 9, 25, 12, 0)
        val empresa = Salon(1L, "Salon", 123L, "salon@test.com", "Calle", 1, "Ciudad")
        val extra = Extra(2L, "Camarera", TipoExtra.TIPO_CATERING)
        empresa.listaPrecioConFechaExtra.add(
            PrecioConFechaExtra(3L, 850.0, fecha.minusDays(1), fecha.plusDays(1), empresa, extra)
        )

        val dto = extra.toExtraPrecioDTO(empresa, fecha)

        assertEquals(2L, dto.id)
        assertEquals("Camarera", dto.nombre)
        assertEquals(TipoExtra.TIPO_CATERING, dto.tipoExtra)
        assertEquals(850.0, dto.precio)
    }

    @Test
    fun `toExtraPrecioDTO usa precio cero si no hay precio vigente`() {
        val fecha = LocalDateTime.of(2026, 9, 25, 12, 0)
        val empresa = Salon(1L, "Salon", 123L, "salon@test.com", "Calle", 1, "Ciudad")
        val extra = Extra(2L, "Camarera", TipoExtra.TIPO_CATERING)

        assertEquals(0.0, extra.toExtraPrecioDTO(empresa, fecha).precio)
    }
}
