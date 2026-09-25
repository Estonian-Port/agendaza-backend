package com.estonianport.agendaza.model.enum

import com.estonianport.agendaza.errors.BusinessException
import com.estonianport.agendaza.model.enums.Duracion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class DuracionTest {

    @Test
    fun `fromString convierte los valores validos`() {
        assertEquals(Duracion.CORTO, Duracion.fromString("CORTO"))
        assertEquals(Duracion.MEDIO, Duracion.fromString("MEDIO"))
        assertEquals(Duracion.LARGO, Duracion.fromString("LARGO"))
    }

    @Test
    fun `fromString ignora mayusculas y minusculas`() {
        assertEquals(Duracion.CORTO, Duracion.fromString("corto"))
        assertEquals(Duracion.MEDIO, Duracion.fromString("MeDiO"))
        assertEquals(Duracion.LARGO, Duracion.fromString("largo"))
    }

    @Test
    fun `fromString lanza BusinessException para un valor no valido`() {
        val exception = assertThrows(BusinessException::class.java) {
            Duracion.fromString("invalido")
        }

        assertEquals("La duración 'invalido' no es válida", exception.message)
    }
}
