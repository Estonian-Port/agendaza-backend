package com.estonianport.agendaza.service

import com.estonianport.agendaza.dto.ExtraDTO
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.PrecioConFechaExtra
import com.estonianport.agendaza.model.Salon
import com.estonianport.agendaza.model.TipoEvento
import com.estonianport.agendaza.model.enums.TipoExtra
import com.estonianport.agendaza.repository.ExtraRepository
import com.estonianport.agendaza.common.toEndOfMonth
import com.estonianport.agendaza.dto.PrecioConFechaDTO
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.Optional

class ExtraServiceTest {

    private val repository = mock<ExtraRepository>()
    private val empresaService = mock<EmpresaService>()
    private val precioService = mock<PrecioConFechaExtraService>()
    private lateinit var service: ExtraService

    @BeforeEach
    fun setUp() {
        service = ExtraService(empresaService, repository, precioService)
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

    @Test
    fun `conteos y listados paginados de evento y catering delegan con pagina de diez`() {
        val pageable = PageRequest.of(1, 10)
        val extraEvento = Extra(1L, "Musica", TipoExtra.EVENTO)
        val extraCatering = Extra(2L, "Menu", TipoExtra.TIPO_CATERING)
        whenever(repository.countEvento(1L)).thenReturn(3)
        whenever(repository.countEventoByNombre(1L, "mus")).thenReturn(1)
        whenever(repository.countCatering(1L)).thenReturn(4)
        whenever(repository.countCateringByNombre(1L, "men")).thenReturn(2)
        whenever(repository.findAllEventoByNombre(1L, "mus", pageable)).thenReturn(PageImpl(listOf(extraEvento)))
        whenever(repository.findAllCatering(1L, pageable)).thenReturn(PageImpl(listOf(extraCatering)))

        assertEquals(3, service.countEvento(1L))
        assertEquals(1, service.countEventoByNombre(1L, "mus"))
        assertEquals(4, service.countCatering(1L))
        assertEquals(2, service.countCateringByNombre(1L, "men"))
        assertEquals("Musica", service.getPageEventoByNombre(1L, 1, "mus").single().nombre)
        assertEquals("Menu", service.getPageCatering(1L, 1).single().nombre)
    }

    @Test
    fun `listas globales y listas para agregar se delegan al repositorio`() {
        val evento = ExtraDTO(1L, "Luces", TipoExtra.EVENTO)
        val catering = ExtraDTO(2L, "Menu", TipoExtra.TIPO_CATERING)
        whenever(repository.getAllEvento()).thenReturn(listOf(evento))
        whenever(repository.getAllCatering()).thenReturn(listOf(catering))
        whenever(repository.getAllExtraEventoAgregar(7L)).thenReturn(listOf(evento))
        whenever(repository.getAllExtraCateringAgregar(7L)).thenReturn(listOf(catering))

        assertEquals(listOf(evento), service.getAllEvento())
        assertEquals(listOf(catering), service.getAllCatering())
        assertEquals(listOf(evento), service.getAllExtraEventoAgregar(7L))
        assertEquals(listOf(catering), service.getAllExtraCateringAgregar(7L))
    }

    @Test
    fun `conversores devuelven precio y filtran extras por tipo`() {
        val empresa = Salon(1L, "Salon", 123L, "salon@test.com", "Calle", 1, "Ciudad")
        val extraEvento = Extra(1L, "Luces", TipoExtra.EVENTO)
        val extraCatering = Extra(2L, "Menu", TipoExtra.TIPO_CATERING)
        val desde = LocalDateTime.of(2026, 1, 1, 0, 0)
        empresa.listaPrecioConFechaExtra.add(PrecioConFechaExtra(1L, 75.0, desde, desde.plusMonths(1), empresa, extraEvento))

        val dto = service.fromListaExtraToListaExtraDto(empresa, listOf(extraEvento), desde.plusDays(2)).single()
        val filtrados = service.fromListaExtraToListaExtraDtoByFilter(
            empresa, mutableSetOf(extraEvento, extraCatering), desde.plusDays(2), TipoExtra.EVENTO
        )

        assertEquals(75.0, dto.precio)
        assertEquals(listOf(extraEvento.id), filtrados.map { it.id })
    }

    @Test
    fun `saveExtra crea el extra lo asocia a tipos y empresa`() {
        val empresa = com.estonianport.agendaza.model.Salon(7L, "Salon", 123L, "salon@test.com", "Calle", 1, "Ciudad")
        val tipoEvento = mock<TipoEvento>()
        val tipoEventoService = mock<TipoEventoService>()
        whenever(empresaService.get(7L)).thenReturn(empresa)
        whenever(empresaService.save(empresa)).thenReturn(empresa)
        whenever(repository.save(any<Extra>())).thenAnswer { it.arguments[0] as Extra }
        whenever(tipoEventoService.get(9L)).thenReturn(tipoEvento)
        val dto = ExtraDTO(0L, "Decoración", TipoExtra.EVENTO).also {
            it.empresaId = 7L
            it.listaTipoEventoId.add(9L)
        }

        val result = service.saveExtra(dto, tipoEventoService)

        assertEquals("Decoración", result.nombre)
        assertEquals(TipoExtra.EVENTO, result.tipoExtra)
        val captor = argumentCaptor<Extra>()
        verify(repository).save(captor.capture())
        assertEquals(setOf(tipoEvento), captor.firstValue.listaTipoEvento)
        assertTrue(empresa.listaExtra.contains(captor.firstValue))
        verify(empresaService).save(empresa)
    }

    @Test
    fun `deleteExtra elimina el vinculo con la empresa`() {
        val empresa = com.estonianport.agendaza.model.Salon(7L, "Salon", 123L, "salon@test.com", "Calle", 1, "Ciudad")
        val conservar = Extra(1L, "Luces", TipoExtra.EVENTO)
        val borrar = Extra(2L, "Sonido", TipoExtra.EVENTO)
        empresa.listaExtra.addAll(listOf(conservar, borrar))
        whenever(empresaService.get(7L)).thenReturn(empresa)
        whenever(empresaService.save(empresa)).thenReturn(empresa)

        service.deleteExtra(2L, 7L)

        assertEquals(setOf(conservar), empresa.listaExtra)
        verify(empresaService).save(empresa)
    }

    @Test
    fun `savePreciosConFecha marca como baja los precios omitidos y guarda el nuevo hasta fin de mes`() {
        val empresa = com.estonianport.agendaza.model.Salon(7L, "Salon", 123L, "salon@test.com", "Calle", 1, "Ciudad")
        val extra = Extra(2L, "Luces", TipoExtra.EVENTO)
        val viejo = PrecioConFechaExtra(11L, 50.0, LocalDateTime.of(2025, 1, 1, 0, 0),
            LocalDateTime.of(2025, 1, 31, 0, 0), empresa, extra)
        empresa.listaPrecioConFechaExtra.add(viejo)
        whenever(repository.findById(2L)).thenReturn(Optional.of(extra))
        whenever(empresaService.get(7L)).thenReturn(empresa)
        whenever(precioService.get(11L)).thenReturn(viejo)
        whenever(precioService.save(any<PrecioConFechaExtra>())).thenAnswer { it.arguments[0] as PrecioConFechaExtra }
        val desde = LocalDateTime.of(2026, 2, 5, 12, 0)
        val hasta = LocalDateTime.of(2026, 2, 20, 12, 0)
        val dto = PrecioConFechaDTO(0L, desde, hasta, 125.0, 7L, 2L)

        service.savePreciosConFecha(7L, 2L, mutableSetOf(dto))

        assertEquals(LocalDate.now(), viejo.fechaBaja)
        verify(precioService).save(viejo)
        val captor = argumentCaptor<PrecioConFechaExtra>()
        verify(precioService, times(2)).save(captor.capture())
        assertEquals(0L, captor.secondValue.id)
        assertEquals(hasta.toEndOfMonth(), captor.secondValue.hasta)
        assertEquals(125.0, captor.secondValue.precio)
    }
}
