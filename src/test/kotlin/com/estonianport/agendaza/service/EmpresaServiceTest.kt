package com.estonianport.agendaza.service

import com.estonianport.agendaza.dto.TipoEventoDTO
import com.estonianport.agendaza.dto.EmpresaDTO
import com.estonianport.agendaza.dto.CantidadesPanelAdminDTO
import com.estonianport.agendaza.dto.PrecioConFechaDTO
import com.estonianport.agendaza.dto.EspecificacionDTO
import com.estonianport.agendaza.errors.BusinessException
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Salon
import com.estonianport.agendaza.model.PrecioDePlatoNinos
import com.estonianport.agendaza.repository.EventoRepository
import com.estonianport.agendaza.repository.CargoRepository
import com.estonianport.agendaza.repository.TipoEventoRepository
import com.estonianport.agendaza.repository.ExtraRepository
import com.estonianport.agendaza.repository.PagoRepository
import com.estonianport.agendaza.repository.ServicioRepository
import com.estonianport.agendaza.repository.ClausulaRepository
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
import org.mockito.kotlin.any
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalTime
import java.time.LocalDateTime
import java.util.Optional

class EmpresaServiceTest {

    private val repository = mock<EmpresaRepository>()
    private val eventoRepository = mock<EventoRepository>()
    private val cargoRepository = mock<CargoRepository>()
    private val tipoEventoRepository = mock<TipoEventoRepository>()
    private val extraRepository = mock<ExtraRepository>()
    private val pagoRepository = mock<PagoRepository>()
    private val servicioRepository = mock<ServicioRepository>()
    private val clausulaRepository = mock<ClausulaRepository>()
    private lateinit var service: EmpresaService

    private val empresa: Empresa = Salon(1L, "Salon", 123L, "salon@test.com", "Calle", 1, "Ciudad")

    @BeforeEach
    fun setUp() {
        service = EmpresaService(
            repository, eventoRepository, cargoRepository, tipoEventoRepository,
            extraRepository, pagoRepository, servicioRepository, clausulaRepository, gastoRepository
        )
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

    @Test
    fun `getEmpresaDTO devuelve el DTO del repositorio`() {
        val dto = EmpresaDTO(1L, "Salon", "salon@test.com", 123L, "Calle", 1, "Ciudad")
        whenever(repository.findDTOById(1L)).thenReturn(Optional.of(dto))

        assertEquals(dto, service.getEmpresaDTO(1L))
    }

    @Test
    fun `getEmpresaDTO lanza NotFoundException si no existe`() {
        whenever(repository.findDTOById(9L)).thenReturn(Optional.empty())

        val error = assertThrows(NotFoundException::class.java) { service.getEmpresaDTO(9L) }

        assertEquals("Empresa con ID 9 no encontrada", error.message)
    }

    @Test
    fun `save actualiza los campos de la empresa preservando su tipo`() {
        val dto = EmpresaDTO(1L, "Nuevo", "nuevo@test.com", 456L, "Avenida", 12, "Madrid")
        whenever(repository.findById(1L)).thenReturn(Optional.of(empresa))
        whenever(repository.save(any<Empresa>())).thenAnswer { it.arguments[0] as Empresa }

        val result = service.save(dto)

        assertEquals(1L, result.id)
        assertEquals("Nuevo", result.nombre)
        val captor = org.mockito.kotlin.argumentCaptor<Empresa>()
        verify(repository).save(captor.capture())
        assertEquals("Nuevo", captor.firstValue.nombre)
        assertEquals(456L, captor.firstValue.telefono)
        assertEquals("nuevo@test.com", captor.firstValue.email)
        assertEquals("Avenida", captor.firstValue.calle)
        assertEquals(12, captor.firstValue.numero)
        assertEquals("Madrid", captor.firstValue.municipio)
        assertEquals(empresa::class, captor.firstValue::class)
    }

    @Test
    fun `save lanza NotFoundException cuando la empresa no existe`() {
        whenever(repository.findById(9L)).thenReturn(Optional.empty())
        val dto = EmpresaDTO(9L, "Nuevo", "n@test.com", 1L, "Calle", 2, "Ciudad")

        assertThrows(NotFoundException::class.java) { service.save(dto) }
        verify(repository, org.mockito.kotlin.never()).save(any<Empresa>())
    }

    @Test
    fun `listados de eventos usan paginas de diez elementos`() {
        val pageable = PageRequest.of(2, 10)
        val eventos = listOf(com.estonianport.agendaza.dto.EventoDTO(1L, "boda", "ABCD", java.time.LocalDateTime.now(), java.time.LocalDateTime.now(), "Fiesta"))
        whenever(eventoRepository.eventosByEmpresa(1L, pageable)).thenReturn(PageImpl(eventos))
        whenever(eventoRepository.eventosByNombre(1L, "boda", pageable)).thenReturn(PageImpl(eventos))

        assertEquals(eventos, service.getAllEventoByEmpresaId(1L, 2))
        assertEquals(eventos, service.getAllEventoByFilterName(1L, 2, "boda"))
    }

    @Test
    fun `getAllCantidadesForPanelAdmin combina los contadores de los repositorios`() {
        whenever(cargoRepository.countActivosByEmpresaId(1L)).thenReturn(1)
        whenever(tipoEventoRepository.countActivosByEmpresaId(1L)).thenReturn(2)
        whenever(extraRepository.countEvento(1L)).thenReturn(3)
        whenever(extraRepository.countCatering(1L)).thenReturn(4)
        whenever(pagoRepository.countActivosByEmpresaId(1L)).thenReturn(5)
        whenever(eventoRepository.countActivosByEmpresaId(1L)).thenReturn(6)
        whenever(eventoRepository.countClientesByEmpresaId(1L)).thenReturn(7)
        whenever(servicioRepository.countActivosByEmpresaId(1L)).thenReturn(8)
        whenever(clausulaRepository.countByEmpresaId(1L)).thenReturn(9)

        assertEquals(CantidadesPanelAdminDTO(1, 2, 3, 5, 6, 7, 4, 8, 9), service.getAllCantidadesForPanelAdminByEmpresaId(1L))
    }

    @Test
    fun `getEspecificaciones convierte las entidades en DTOs`() {
        val especificacion = PrecioDePlatoNinos(1L, empresa, 50)
        whenever(repository.getEspecificaciones(1L)).thenReturn(listOf(especificacion))

        assertEquals(listOf(EspecificacionDTO("El precio del plato para niños representa un % del varor del plato", "Porcentaje: 50%")), service.getEspecificaciones(1L))
    }

    @Test
    fun `consultas de precios delegan sus identificadores al repositorio`() {
        val desde = LocalDateTime.of(2026, 1, 1, 0, 0)
        val hasta = desde.plusDays(1)
        val preciosExtra = listOf(PrecioConFechaDTO(1L, desde, hasta, 100.0, 1L, 2L))
        val preciosTipo = listOf(PrecioConFechaDTO(3L, desde, hasta, 200.0, 1L, 4L))
        whenever(repository.getAllPrecioConFechaByExtraId(1L, 2L)).thenReturn(preciosExtra)
        whenever(repository.getAllPrecioConFechaByTipoEventoId(1L, 4L)).thenReturn(preciosTipo)

        assertEquals(preciosExtra, service.getAllPrecioConFechaByExtraId(1L, 2L))
        assertEquals(preciosTipo, service.getAllPrecioConFechaByTipoEvento(1L, 4L))
    }
}
