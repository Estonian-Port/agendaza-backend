package com.estonianport.agendaza.service

import com.estonianport.agendaza.dto.AgendaDTO
import com.estonianport.agendaza.model.Cargo
import com.estonianport.agendaza.model.Salon
import com.estonianport.agendaza.model.Usuario
import com.estonianport.agendaza.model.enums.TipoCargo
import com.estonianport.agendaza.repository.CargoRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional

class CargoServiceTest {

    private val repository = mock<CargoRepository>()
    private lateinit var service: CargoService

    private val usuario = Usuario(2L, "Ana", "Pérez", 112233L, "ana@test.com")
    private val empresa = Salon(3L, "Salon", 123L, "salon@test.com", "Calle", 1, "Ciudad")
    private val cargo = Cargo(4L, usuario, empresa, TipoCargo.ENCARGADO)

    @BeforeEach
    fun setUp() {
        service = CargoService().also { it.cargoRepository = repository }
    }

    @Test
    fun `findById devuelve el cargo encontrado`() {
        whenever(repository.findById(4L)).thenReturn(Optional.of(cargo))

        assertSame(cargo, service.findById(4L))
    }

    @Test
    fun `findById falla si no existe el cargo`() {
        whenever(repository.findById(99L)).thenReturn(Optional.empty())

        assertThrows(NoSuchElementException::class.java) { service.findById(99L) }
    }

    @Test
    fun `getListaCargosByUsuarioId devuelve los resultados del repositorio`() {
        val cargos = listOf(AgendaDTO(3L, "Salon", TipoCargo.ENCARGADO))
        whenever(repository.getListaCargosByUsuarioId(2L)).thenReturn(cargos)

        assertEquals(cargos, service.getListaCargosByUsuarioId(2L))
        verify(repository).getListaCargosByUsuarioId(2L)
    }

    @Test
    fun `getCargoByEmpresaIdAndUsuarioId delega la busqueda`() {
        whenever(repository.getCargoByEmpresaIdAndUsuarioId(3L, 2L)).thenReturn(cargo)

        assertSame(cargo, service.getCargoByEmpresaIdAndUsuarioId(3L, 2L))
    }

    @Test
    fun `delete marca el cargo con fecha de baja y lo guarda`() {
        whenever(repository.getCargoByEmpresaIdAndUsuarioId(3L, 2L)).thenReturn(cargo)

        service.delete(3L, 2L)

        assertNotNull(cargo.fechaBaja)
        verify(repository).save(cargo)
    }

    @Test
    fun `delete no cambia la fecha si la busqueda falla`() {
        whenever(repository.getCargoByEmpresaIdAndUsuarioId(3L, 2L)).thenThrow(NoSuchElementException())

        assertThrows(NoSuchElementException::class.java) { service.delete(3L, 2L) }

        assertNull(cargo.fechaBaja)
    }
}
