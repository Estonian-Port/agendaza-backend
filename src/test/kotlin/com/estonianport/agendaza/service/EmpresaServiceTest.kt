package com.estonianport.agendaza.service

import com.estonianport.agendaza.dto.TipoEventoDTO
import com.estonianport.agendaza.errors.BusinessException
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Salon
import com.estonianport.agendaza.model.enums.Duracion
import com.estonianport.agendaza.repository.EmpresaRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalTime
import java.util.Optional

class EmpresaServiceTest {

    private val repository = mock<EmpresaRepository>()
    private lateinit var service: EmpresaService

    private val empresa: Empresa = Salon(1L, "Salon", 123L, "salon@test.com", "Calle", 1, "Ciudad")

    @BeforeEach
    fun setUp() {
        service = EmpresaService().also {
            ReflectionTestUtils.setField(it, "empresaRepository", repository)
        }
    }

    @Test
    fun `findById devuelve la empresa cuando existe`() {
        whenever(repository.findById(1L)).thenReturn(Optional.of(empresa))

        assertSame(empresa, service.findById(1L))
    }

    @Test
    fun `findById lanza NotFoundException cuando no existe`() {
        whenever(repository.findById(99L)).thenReturn(Optional.empty())

        val error = assertThrows(NotFoundException::class.java) { service.findById(99L) }

        assertEquals("Empresa no encontrada con el ID: 99", error.message)
    }

    @Test
    fun `getTiposEventoByDuracion convierte el texto y consulta el repositorio`() {
        val esperado = listOf(
            TipoEventoDTO(3L, "Fiesta", LocalTime.of(5, 0), Duracion.LARGO, 80, 10, 1L)
        )
        whenever(repository.findByEmpresaIdAndDuracion(1L, Duracion.LARGO)).thenReturn(esperado)

        assertEquals(esperado, service.getTiposEventoByDuracion(1L, "largo"))
        verify(repository).findByEmpresaIdAndDuracion(1L, Duracion.LARGO)
    }

    @Test
    fun `getTiposEventoByDuracion lanza BusinessException para duracion invalida`() {
        assertThrows(BusinessException::class.java) {
            service.getTiposEventoByDuracion(1L, "invalida")
        }
    }
}
