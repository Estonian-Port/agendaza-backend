package com.estonianport.agendaza.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class UsuarioTest {

    @Test
    fun `inicializa valores opcionales y coleccion vacia`() {
        val antesDeCrear = LocalDate.now()

        val usuario = Usuario(1L, "Ana", "Pérez", 112233L, "ana@test.com")
        val despuesDeCrear = LocalDate.now()

        assertNull(usuario.username)
        assertNull(usuario.password)
        assertNull(usuario.fechaBaja)
        assertTrue(usuario.listaCargo.isEmpty())
        assertTrue(usuario.fechaNacimiento in antesDeCrear..despuesDeCrear)
        assertTrue(usuario.fechaAlta in antesDeCrear..despuesDeCrear)
    }

    @Test
    fun `permite actualizar datos de usuario`() {
        val usuario = Usuario(1L, "Ana", "Pérez", 112233L, "ana@test.com")
        val nacimiento = LocalDate.of(1995, 4, 20)

        usuario.nombre = "Ana María"
        usuario.username = "ana"
        usuario.password = "hash"
        usuario.fechaNacimiento = nacimiento

        assertEquals("Ana María", usuario.nombre)
        assertEquals("ana", usuario.username)
        assertEquals("hash", usuario.password)
        assertEquals(nacimiento, usuario.fechaNacimiento)
    }
}
