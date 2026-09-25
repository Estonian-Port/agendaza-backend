package com.estonianport.agendaza.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ClausulaTest {

    @Test
    fun `toDTO copia identificador y nombre`() {
        val clausula = Clausula(9L, "Cancelación")

        val dto = clausula.toDTO()

        assertEquals(9L, dto.id)
        assertEquals("Cancelación", dto.nombre)
    }

    @Test
    fun `una clausula nueva no tiene fecha de baja y comienza sin empresas`() {
        val clausula = Clausula(9L, "Cancelación")

        assertNull(clausula.fechaBaja)
        assertEquals(emptySet<Empresa>(), clausula.listaEmpresa)
    }
}
