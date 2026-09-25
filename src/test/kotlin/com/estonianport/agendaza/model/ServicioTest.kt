package com.estonianport.agendaza.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ServicioTest {

    @Test
    fun `toDTO copia id y nombre`() {
        val servicio = Servicio(12L, "DJ")

        val dto = servicio.toDTO()

        assertEquals(12L, dto.id)
        assertEquals("DJ", dto.nombre)
    }
}
