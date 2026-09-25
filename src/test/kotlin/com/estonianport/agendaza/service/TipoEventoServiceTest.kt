package com.estonianport.agendaza.service

import com.estonianport.agendaza.dto.TimeDTO
import com.estonianport.agendaza.dto.PrecioConFechaDTO
import com.estonianport.agendaza.dto.TipoEventoDTO
import com.estonianport.agendaza.dto.TipoEventoPrecioDTO
import com.estonianport.agendaza.dto.ExtraPrecioDTO
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.PrecioConFechaTipoEvento
import com.estonianport.agendaza.model.Salon
import com.estonianport.agendaza.model.TipoEvento
import com.estonianport.agendaza.model.enums.Duracion
import com.estonianport.agendaza.model.enums.TipoExtra
import com.estonianport.agendaza.repository.TipoEventoRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.kotlin.eq
import java.time.LocalTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional

class TipoEventoServiceTest {

    private val repository = mock<TipoEventoRepository>()
    private val empresaService = mock<EmpresaService>()
    private val extraService = mock<ExtraService>()
    private val precioService = mock<PrecioConFechaTipoEventoService>()
    private lateinit var service: TipoEventoService

    private val empresa: Empresa = Salon(10L, "Salon", 123L, "salon@test.com", "Calle", 1, "Ciudad")
    private val tipoEvento = TipoEvento(
        1L, "Cumpleaños", Duracion.MEDIO, 100, 20, LocalTime.of(4, 30), empresa
    )

    @BeforeEach
    fun setUp() {
        service = TipoEventoService(repository, empresaService, extraService, precioService)
    }

    @Test
    fun `getDuracion devuelve la duracion configurada`() {
        whenever(repository.findById(1L)).thenReturn(Optional.of(tipoEvento))

        assertEquals(LocalTime.of(4, 30), service.getDuracion(1L))
    }

    @Test
    fun `calcularHoraFin suma la hora de inicio y la duracion`() {
        whenever(repository.findById(1L)).thenReturn(Optional.of(tipoEvento))

        assertEquals(LocalTime.of(23, 45), service.calcularHoraFin(1L, TimeDTO(19, 15)))
    }

    @Test
    fun `getCapacidad devuelve adultos y ninos del tipo de evento`() {
        whenever(repository.findById(1L)).thenReturn(Optional.of(tipoEvento))

        val capacidad = service.getCapacidad(1L)

        assertEquals(100, capacidad.capacidadAdultos)
        assertEquals(20, capacidad.capacidadNinos)
    }

    @Test
    fun `findExtraNino devuelve el extra llamado Nino`() {
        val extraNino = Extra(2L, "Niño", TipoExtra.EVENTO)
        tipoEvento.listaExtra.add(Extra(3L, "Camarera principal", TipoExtra.EVENTO))
        tipoEvento.listaExtra.add(extraNino)
        whenever(repository.findById(1L)).thenReturn(Optional.of(tipoEvento))

        assertEquals(extraNino, service.findExtraNino(1L))
    }

    @Test
    fun `findExtraCamarera devuelve el primer extra cuyo nombre empieza con Camarera`() {
        val extra = Extra(4L, "Camarera adicional", TipoExtra.EVENTO)
        tipoEvento.listaExtra.add(extra)
        whenever(repository.findById(1L)).thenReturn(Optional.of(tipoEvento))

        assertEquals(extra, service.findExtraCamarera(1L))
    }

    @Test
    fun `findExtraNino devuelve null si el tipo de evento no tiene ese extra`() {
        whenever(repository.findById(1L)).thenReturn(Optional.of(tipoEvento))

        assertNull(service.findExtraNino(1L))
    }

    @Test
    fun `getDuracion lanza NotFoundException si no existe el tipo de evento`() {
        whenever(repository.findById(99L)).thenReturn(Optional.empty())

        assertThrows(NotFoundException::class.java) { service.getDuracion(99L) }
    }

    @Test
    fun `saveTipoEvento crea un tipo de evento y devuelve su DTO`() {
        whenever(empresaService.get(empresa.id)).thenReturn(empresa)
        whenever(repository.save(any<TipoEvento>())).thenAnswer { invocation ->
            invocation.getArgument<TipoEvento>(0).apply { id = 12L }
        }
        val dto = TipoEventoDTO(0L, "Fiesta", LocalTime.of(5, 15), Duracion.LARGO, 80, 15, empresa.id)

        val resultado = service.saveTipoEvento(dto)

        assertEquals(12L, resultado.id)
        assertEquals("Fiesta", resultado.nombre)
        assertEquals(LocalTime.of(5, 15), resultado.cantidadDuracion)
        assertEquals(Duracion.LARGO, resultado.duracion)
        verify(repository).save(any<TipoEvento>())
    }

    @Test
    fun `deleteTipoEvento setea fecha de baja y guarda el tipo`() {
        whenever(repository.findById(1L)).thenReturn(Optional.of(tipoEvento))
        whenever(repository.save(tipoEvento)).thenReturn(tipoEvento)

        val resultado = service.deleteTipoEvento(1L)

        assertNotNull(tipoEvento.fechaBaja)
        assertEquals(tipoEvento.id, resultado.id)
        verify(repository).save(tipoEvento)
    }

    @Test
    fun `savePreciosConFecha da de baja precios omitidos y guarda nuevos hasta fin de mes`() {
        val precioViejo = PrecioConFechaTipoEvento(
            20L, 100.0, LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 12, 31, 23, 59), empresa, tipoEvento
        )
        empresa.listaPrecioConFechaTipoEvento.add(precioViejo)
        whenever(repository.findById(1L)).thenReturn(Optional.of(tipoEvento))
        whenever(empresaService.get(empresa.id)).thenReturn(empresa)
        whenever(precioService.get(20L)).thenReturn(precioViejo)
        whenever(precioService.save(any<PrecioConFechaTipoEvento>())).thenAnswer { invocation ->
            invocation.getArgument<PrecioConFechaTipoEvento>(0)
        }
        val nuevoDesde = LocalDateTime.of(2026, 9, 1, 0, 0)
        val nuevoHasta = LocalDateTime.of(2026, 9, 25, 18, 0)
        val nuevos = mutableSetOf(PrecioConFechaDTO(21L, nuevoDesde, nuevoHasta, 250.0, empresa.id, tipoEvento.id))

        service.savePreciosConFecha(empresa.id, tipoEvento.id, nuevos)

        assertNotNull(precioViejo.fechaBaja)
        val preciosGuardados = argumentCaptor<PrecioConFechaTipoEvento>()
        verify(precioService, org.mockito.kotlin.times(2)).save(preciosGuardados.capture())
        assertEquals(LocalDate.now(), preciosGuardados.firstValue.fechaBaja)
        val precioNuevo = preciosGuardados.secondValue
        assertEquals(21L, precioNuevo.id)
        assertEquals(250.0, precioNuevo.precio)
        assertEquals(LocalDateTime.of(2026, 9, 30, 23, 59, 59), precioNuevo.hasta)
    }

    @Test
    fun `getPrecio devuelve cero cuando no hay precio para la fecha`() {
        val fecha = LocalDateTime.of(2026, 9, 25, 12, 0)
        whenever(repository.getTipoEventoConPrecio(empresa.id, tipoEvento.id, fecha)).thenReturn(null)

        assertEquals(0.0, service.getPrecio(empresa.id, tipoEvento.id, fecha))
    }

    @Test
    fun `getPrecio devuelve el precio consultado`() {
        val fecha = LocalDateTime.of(2026, 9, 25, 12, 0)
        whenever(repository.getTipoEventoConPrecio(empresa.id, tipoEvento.id, fecha))
            .thenReturn(TipoEventoPrecioDTO(tipoEvento.id, tipoEvento.nombre, 450.0))

        assertEquals(450.0, service.getPrecio(empresa.id, tipoEvento.id, fecha))
    }

    @Test
    fun `getExtrasConPrecio delega el filtro al servicio de extras`() {
        val fecha = LocalDateTime.of(2026, 9, 25, 12, 0)
        val esperado = listOf(ExtraPrecioDTO(2L, "Mozo", TipoExtra.VARIABLE_EVENTO, 100.0))
        whenever(extraService.getAllExtraConPrecioByTipoEventoAndFecha(
            empresa.id, tipoEvento.id, fecha, TipoExtra.VARIABLE_EVENTO
        )).thenReturn(esperado)

        assertEquals(esperado, service.getExtrasConPrecio(empresa.id, tipoEvento.id, fecha, TipoExtra.VARIABLE_EVENTO))
        verify(extraService).getAllExtraConPrecioByTipoEventoAndFecha(
            empresa.id, tipoEvento.id, fecha, TipoExtra.VARIABLE_EVENTO
        )
    }
}
