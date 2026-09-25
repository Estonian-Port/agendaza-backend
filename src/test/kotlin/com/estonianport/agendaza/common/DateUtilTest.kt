package com.estonianport.agendaza.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class DateUtilTest {

    @Test
    fun `toEndOfMonth devuelve el ultimo segundo del mes`() {
        val fecha = LocalDateTime.of(2026, 9, 12, 8, 30, 15)

        assertEquals(LocalDateTime.of(2026, 9, 30, 23, 59, 59), fecha.toEndOfMonth())
    }

    @Test
    fun `toEndOfMonth respeta febrero en ano bisiesto`() {
        val fecha = LocalDateTime.of(2024, 2, 3, 10, 0)

        assertEquals(LocalDateTime.of(2024, 2, 29, 23, 59, 59), fecha.toEndOfMonth())
    }
}
