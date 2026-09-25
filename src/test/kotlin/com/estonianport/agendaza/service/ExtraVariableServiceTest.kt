package com.estonianport.agendaza.service

import com.estonianport.agendaza.dto.EventoExtraVariableDTO
import com.estonianport.agendaza.model.EventoExtraVariable
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.PrecioConFechaExtra
import com.estonianport.agendaza.model.Salon
import com.estonianport.agendaza.model.enums.TipoExtra
import com.estonianport.agendaza.repository.ExtraVariableRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

class ExtraVariableServiceTest {

    private val repository = mock<ExtraVariableRepository>()
    private val extraService = mock<ExtraService>()
    private lateinit var service: ExtraVariableService

    private val fecha = LocalDateTime.of(2026, 9, 25, 12, 0)
    private val empresa = Salon(1L, "Salon", 123L, "salon@test.com", "Calle", 1, "Ciudad")

    @BeforeEach
    fun setUp() {
        service = ExtraVariableService().also {
            it.extraVariableRepository = repository
            it.extraService = extraService
        }
    }

    @Test
    fun `fromListaExtraVariableDtoToListaExtraVariable obtiene el extra y conserva la cantidad`() {
        val extra = Extra(2L, "Mozo", TipoExtra.VARIABLE_EVENTO)
        whenever(extraService.get(2L)).thenReturn(extra)

        val resultado = service.fromListaExtraVariableDtoToListaExtraVariable(
            listOf(EventoExtraVariableDTO(2L, 3, "Mozo", 120.0))
        )

        assertEquals(1, resultado.size)
        assertEquals(0L, resultado.single().id)
        assertEquals(extra, resultado.single().extra)
        assertEquals(3, resultado.single().cantidad)
    }

    @Test
    fun `convierte extras variables a DTO calculando precio por cantidad`() {
        val extra = Extra(2L, "Mozo", TipoExtra.VARIABLE_EVENTO)
        val eventoExtra = EventoExtraVariable(5L, extra, 3)
        empresa.listaPrecioConFechaExtra.add(
            PrecioConFechaExtra(8L, 40.0, fecha.minusDays(1), fecha.plusDays(1), empresa, extra)
        )

        val resultado = service.fromListaExtraVariableToListaExtraVariableDto(empresa, listOf(eventoExtra), fecha)

        assertEquals(1, resultado.size)
        assertEquals(2L, resultado.single().id)
        assertEquals("Mozo", resultado.single().nombre)
        assertEquals(3, resultado.single().cantidad)
        assertEquals(120.0, resultado.single().precio)
    }

    @Test
    fun `el filtrado por tipo extra omite elementos de otros tipos`() {
        val extraEvento = Extra(2L, "Mozo", TipoExtra.VARIABLE_EVENTO)
        val extraCatering = Extra(3L, "Camarera", TipoExtra.VARIABLE_CATERING)
        val evento = EventoExtraVariable(5L, extraEvento, 2)
        val catering = EventoExtraVariable(6L, extraCatering, 1)
        empresa.listaPrecioConFechaExtra.add(
            PrecioConFechaExtra(8L, 25.0, fecha.minusDays(1), fecha.plusDays(1), empresa, extraEvento)
        )
        empresa.listaPrecioConFechaExtra.add(
            PrecioConFechaExtra(9L, 70.0, fecha.minusDays(1), fecha.plusDays(1), empresa, extraCatering)
        )

        val resultado = service.fromListaExtraVariableToListaExtraVariableDtoByFilter(
            empresa, mutableSetOf(evento, catering), fecha, TipoExtra.VARIABLE_CATERING
        )

        assertEquals(1, resultado.size)
        assertEquals("Camarera", resultado.single().nombre)
        assertEquals(70.0, resultado.single().precio)
    }

    @Test
    fun `sin precio vigente convierte el precio a cero`() {
        val eventoExtra = EventoExtraVariable(5L, Extra(2L, "Mozo", TipoExtra.VARIABLE_EVENTO), 3)

        val resultado = service.fromListaExtraVariableToListaExtraVariableDto(empresa, listOf(eventoExtra), fecha)

        assertEquals(0.0, resultado.single().precio)
    }
}
