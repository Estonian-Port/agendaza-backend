package com.estonianport.agendaza.service

import com.estonianport.agendaza.dto.ExtraDTO
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.enums.TipoExtra
import com.estonianport.agendaza.repository.ExtraRepository
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
import java.util.Optional

class ExtraServiceTest {

    private val repository = mock<ExtraRepository>()
    private lateinit var service: ExtraService

    @BeforeEach
    fun setUp() {
        service = ExtraService().also { it.extraRepository = repository }
    }

    @Test
    fun `fromListaExtraDtoToListaExtra busca los extras por sus ids`() {
        val evento = Extra(1L, "Decoración", TipoExtra.EVENTO)
        val variable = Extra(2L, "Mozo", TipoExtra.VARIABLE_EVENTO)
        whenever(repository.findById(1L)).thenReturn(Optional.of(evento))
        whenever(repository.findById(2L)).thenReturn(Optional.of(variable))

        val resultado = service.fromListaExtraDtoToListaExtra(
            listOf(ExtraDTO(1L, "Decoración", TipoExtra.EVENTO), ExtraDTO(2L, "Mozo", TipoExtra.VARIABLE_EVENTO))
        )

        assertEquals(listOf(evento, variable), resultado)
        verify(repository).findById(1L)
        verify(repository).findById(2L)
    }

    @Test
    fun `getPageEvento convierte los resultados de la pagina a DTO`() {
        val extra = Extra(3L, "Animación", TipoExtra.EVENTO)
        whenever(repository.findAllEvento(eq(7L), any())).thenReturn(PageImpl(listOf(extra)))

        val resultado = service.getPageEvento(7L, 2)

        assertEquals(1, resultado.size)
        assertEquals(3L, resultado.single().id)
        assertEquals("Animación", resultado.single().nombre)
        assertEquals(TipoExtra.EVENTO, resultado.single().tipoExtra)
        verify(repository).findAllEvento(7L, PageRequest.of(2, 10))
    }

    @Test
    fun `getPageCateringByNombre filtra por nombre y pagina`() {
        val extra = Extra(4L, "Menú infantil", TipoExtra.TIPO_CATERING)
        whenever(repository.findAllCateringByNombre(eq(8L), eq("infantil"), any()))
            .thenReturn(PageImpl(listOf(extra)))

        val resultado = service.getPageCateringByNombre(8L, 1, "infantil")

        assertEquals(listOf("Menú infantil"), resultado.map { it.nombre })
        verify(repository).findAllCateringByNombre(8L, "infantil", PageRequest.of(1, 10))
    }

    @Test
    fun `countEvento delega el conteo al repositorio`() {
        whenever(repository.countEvento(5L)).thenReturn(12)

        assertEquals(12, service.countEvento(5L))
        verify(repository).countEvento(5L)
    }

    @Test
    fun `getAllExtraConPrecio delega los parametros al repositorio`() {
        val fecha = java.time.LocalDateTime.of(2026, 9, 25, 10, 0)
        val esperado = emptyList<com.estonianport.agendaza.dto.ExtraPrecioDTO>()
        whenever(repository.getAllExtraConPrecioByTipoEventoAndFecha(5L, 6L, fecha, TipoExtra.VARIABLE_CATERING))
            .thenReturn(esperado)

        val extras = service.getAllExtraConPrecioByTipoEventoAndFecha(5L, 6L, fecha, TipoExtra.VARIABLE_CATERING)

        assertEquals(esperado, extras)
        verify(repository).getAllExtraConPrecioByTipoEventoAndFecha(5L, 6L, fecha, TipoExtra.VARIABLE_CATERING)
    }
}
