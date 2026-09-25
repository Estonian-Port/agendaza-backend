package com.estonianport.agendaza.service

import com.estonianport.agendaza.dto.GenericItemDTO
import com.estonianport.agendaza.model.Clausula
import com.estonianport.agendaza.model.Salon
import com.estonianport.agendaza.repository.ClausulaRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.util.ReflectionTestUtils

class ClausulaServiceTest {

    private val repository = mock<ClausulaRepository>()
    private val empresaService = mock<EmpresaService>()
    private lateinit var service: ClausulaService

    private val empresa = Salon(1L, "Salon", 123L, "salon@test.com", "Calle", 1, "Ciudad")

    @BeforeEach
    fun setUp() {
        service = ClausulaService().also {
            it.clausulaRepository = repository
            ReflectionTestUtils.setField(it, "empresaService", empresaService)
        }
    }

    @Test
    fun `getAll devuelve el contenido de la pagina solicitada`() {
        val esperado = listOf(GenericItemDTO(2L, "Cancelación"))
        whenever(repository.getAll(eq(1L), any())).thenReturn(PageImpl(esperado))

        assertEquals(esperado, service.getAll(1L, 3))
        verify(repository).getAll(1L, PageRequest.of(3, 10))
    }

    @Test
    fun `getAllFiltro pasa el texto y la pagina al repositorio`() {
        val esperado = listOf(GenericItemDTO(4L, "Seña"))
        whenever(repository.getAllFiltro(eq(1L), eq("seña"), any())).thenReturn(PageImpl(esperado))

        assertEquals(esperado, service.getAllFiltro(1L, "seña", 0))
        verify(repository).getAllFiltro(1L, "seña", PageRequest.of(0, 10))
    }

    @Test
    fun `getAllCantidad delega el conteo al repositorio`() {
        whenever(repository.getAllCantidad(1L)).thenReturn(6)

        assertEquals(6, service.getAllCantidad(1L))
    }

    @Test
    fun `getAllCantidadFiltro delega el texto de busqueda`() {
        whenever(repository.getAllCantidadFiltro(1L, "reserva")).thenReturn(2)

        assertEquals(2, service.getAllCantidadFiltro(1L, "reserva"))
    }

    @Test
    fun `getAllAgregar devuelve las clausulas disponibles`() {
        val disponibles = listOf(GenericItemDTO(8L, "Uso del salón"))
        whenever(repository.getAllAgregar(1L)).thenReturn(disponibles)

        assertEquals(disponibles, service.getAllAgregar(1L))
    }

    @Test
    fun `delete quita la clausula de la empresa y guarda los cambios`() {
        empresa.listaClausula.add(Clausula(10L, "A cancelar"))
        empresa.listaClausula.add(Clausula(11L, "Otra cláusula"))
        whenever(empresaService.get(1L)).thenReturn(empresa)

        service.delete(10L, 1L)

        assertEquals(listOf(11L), empresa.listaClausula.map { it.id })
        verify(empresaService).save(empresa)
    }
}
