package com.estonianport.agendaza.service

import com.estonianport.agendaza.dto.GenericItemDTO
import com.estonianport.agendaza.dto.ServicioDTO
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.Salon
import com.estonianport.agendaza.model.Servicio
import com.estonianport.agendaza.model.TipoEvento
import com.estonianport.agendaza.model.enums.Duracion
import com.estonianport.agendaza.repository.ServicioRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageRequest
import java.time.LocalTime
import java.util.Optional

class ServicioServiceTest {

    private val repository = mock<ServicioRepository>()
    private val empresaService = mock<EmpresaService>()
    private val tipoEventoService = mock<TipoEventoService>()
    private lateinit var service: ServicioService

    private val empresa = Salon(1L, "Salon", 123L, "salon@test.com", "Calle", 1, "Ciudad")
    private val tipoEvento = TipoEvento(4L, "Fiesta", Duracion.MEDIO, 50, 10, LocalTime.of(4, 0), empresa)

    @BeforeEach
    fun setUp() {
        service = ServicioService(repository, empresaService, tipoEventoService)
    }

    @Test
    fun `getServicioConTiposEvento incluye los ids de tipos asociados`() {
        val servicio = Servicio(2L, "DJ")
        empresa.listaServicio.add(servicio)
        whenever(repository.findById(2L)).thenReturn(Optional.of(servicio))
        whenever(repository.getTipoEventoIdsByServicioAndEmpresa(2L, empresa.id))
            .thenReturn(listOf(4L))

        val resultado = service.getServicioConTiposEvento(2L, empresa.id)

        assertEquals(2L, resultado.id)
        assertEquals("DJ", resultado.nombre)
        assertEquals(listOf(4L), resultado.listaTipoEventoId)
    }

    @Test
    fun `getServicioConTiposEvento lanza NotFoundException si no existe`() {
        whenever(repository.findById(99L)).thenReturn(Optional.empty())

        assertThrows(NotFoundException::class.java) {
            service.getServicioConTiposEvento(99L, empresa.id)
        }
    }

    @Test
    fun `getPage solicita la pagina y devuelve su contenido`() {
        val esperado = listOf(ServicioDTO(3L, "DJ"))
        whenever(repository.getPage(eq(1L), anyOrNull(), any())).thenReturn(esperado)

        assertEquals(esperado, service.getPage(1L, 2, null))
        verify(repository).getPage(1L, null, PageRequest.of(2, 10))
    }

    @Test
    fun `saveServicio crea servicio y lo agrega a la empresa`() {
        val dto = GenericItemDTO(0L, "DJ").apply {
            empresaId = empresa.id
            listaTipoEventoId.add(tipoEvento.id)
        }
        whenever(
            tipoEventoService.getAllByIdsAndEmpresa(dto.listaTipoEventoId, empresa.id)
        ).thenReturn(listOf(tipoEvento))
        whenever(repository.save(any<Servicio>())).thenAnswer { invocation ->
            invocation.getArgument<Servicio>(0).apply { id = 8L }
        }
        whenever(empresaService.findById(empresa.id)).thenReturn(empresa)

        val resultado = service.saveServicio(dto)

        assertEquals(8L, resultado.id)
        assertEquals("dj", resultado.nombre)
        assertEquals(listOf(tipoEvento.id), resultado.listaTipoEventoId)
        assertEquals(8L, empresa.listaServicio.single().id)
        verify(empresaService).save(empresa)
    }

    @Test
    fun `deleteService quita el servicio de la empresa y guarda los cambios`() {
        empresa.listaServicio.add(Servicio(5L, "DJ"))
        empresa.listaServicio.add(Servicio(6L, "Fotografía"))
        whenever(empresaService.get(empresa.id)).thenReturn(empresa)

        service.deleteService(5L, empresa.id)

        assertEquals(listOf(6L), empresa.listaServicio.map { it.id })
        verify(empresaService).save(empresa)
    }
}
