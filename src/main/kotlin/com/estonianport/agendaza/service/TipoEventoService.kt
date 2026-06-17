package com.estonianport.agendaza.service

import com.estonianport.agendaza.common.GenericServiceImpl
import com.estonianport.agendaza.common.toEndOfMonth
import com.estonianport.agendaza.dto.ExtraPrecioDTO
import com.estonianport.agendaza.dto.PrecioConFechaDTO
import com.estonianport.agendaza.dto.TipoEventoDTO
import com.estonianport.agendaza.dto.TipoEventoPrecioDTO
import com.estonianport.agendaza.dto.TimeDTO
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.Capacidad
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.PrecioConFechaTipoEvento
import com.estonianport.agendaza.model.TipoEvento
import com.estonianport.agendaza.model.enums.TipoExtra
import com.estonianport.agendaza.repository.TipoEventoRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class TipoEventoService(
    private val tipoEventoRepository: TipoEventoRepository,
    private val capacidadService: CapacidadService,
    private val empresaService: EmpresaService,
    private val extraService: ExtraService,
    private val precioConFechaTipoEventoService: PrecioConFechaTipoEventoService
) : GenericServiceImpl<TipoEvento, Long>() {

    override val dao: CrudRepository<TipoEvento, Long>
        get() = tipoEventoRepository

    // ==================== QUERIES ====================

    @Transactional(readOnly = true)
    fun getTipoEventoDTO(id: Long): TipoEventoDTO {
        return tipoEventoRepository.findById(id)
            .orElseThrow { NotFoundException("TipoEvento no encontrado con id: $id") }
            .toDTO()
    }

    @Transactional(readOnly = true)
    fun getAllByExtra(extraId: Long): MutableList<TipoEventoDTO> {
        return tipoEventoRepository.getAllByExtra(extraId)
    }

    @Transactional(readOnly = true)
    fun getAllByServicio(servicioId: Long): MutableList<TipoEventoDTO> {
        return tipoEventoRepository.getAllByServicio(servicioId)
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["tipos-evento"], key = "#empresaId + '-all'")
    fun getAllTipoEventoByEmpresaId(empresaId: Long): List<TipoEventoDTO> {
        return tipoEventoRepository.getAllTipoEventoByEmpresaId(empresaId)
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["tipos-evento"], key = "#empresaId + '-' + #pageNumber")
    fun getAllTipoEventoByEmpresaId(empresaId: Long, pageNumber: Int): List<TipoEventoDTO> {
        return tipoEventoRepository
            .getAllTipoEventoByEmpresaId(empresaId, PageRequest.of(pageNumber, 10))
            .content
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["tipos-evento-count"], key = "#empresaId")
    fun getCantidadTipoEvento(empresaId: Long): Int {
        return tipoEventoRepository.getCantidadTipoEvento(empresaId)
    }

    @Transactional(readOnly = true)
    fun getAllTipoEventoFilterNombre(empresaId: Long, buscar: String, pageNumber: Int): List<TipoEventoDTO> {
        return tipoEventoRepository
            .getAllTipoEventoFilterNombre(empresaId, buscar, PageRequest.of(pageNumber, 10))
            .content
    }

    @Transactional(readOnly = true)
    fun getCantidadTipoEventoFiltrados(empresaId: Long, buscar: String): Int {
        return tipoEventoRepository.getCantidadTipoEventoFiltrados(empresaId, buscar)
    }

    @Transactional(readOnly = true)
    fun getTipoEventoConPrecio(empresaId: Long, tipoEventoId: Long, fechaEvento: LocalDateTime): TipoEventoPrecioDTO? {
        return tipoEventoRepository.getTipoEventoConPrecio(empresaId, tipoEventoId, fechaEvento)
    }

    @Transactional(readOnly = true)
    fun getDuracion(id: Long): LocalTime {
        return tipoEventoRepository.findById(id)
            .orElseThrow { NotFoundException("TipoEvento no encontrado con id: $id") }
            .cantidadDuracion
    }

    @Transactional(readOnly = true)
    fun getCapacidad(id: Long): Capacidad {
        return tipoEventoRepository.findById(id)
            .orElseThrow { NotFoundException("TipoEvento no encontrado con id: $id") }
            .capacidad
    }

    @Transactional(readOnly = true)
    fun calcularHoraFin(id: Long, timeStart: TimeDTO): LocalTime {
        val duracion = tipoEventoRepository.findById(id)
            .orElseThrow { NotFoundException("TipoEvento no encontrado con id: $id") }
            .cantidadDuracion
        return duracion
            .plusHours(timeStart.hour.toLong())
            .plusMinutes(timeStart.minute.toLong())
    }

    @Transactional(readOnly = true)
    fun findExtraNino(id: Long): Extra? {
        return tipoEventoRepository.findById(id)
            .orElseThrow { NotFoundException("TipoEvento no encontrado con id: $id") }
            .listaExtra.find { it.nombre == "Niño" }
    }

    @Transactional(readOnly = true)
    fun findExtraCamarera(id: Long): Extra? {
        return tipoEventoRepository.findById(id)
            .orElseThrow { NotFoundException("TipoEvento no encontrado con id: $id") }
            .listaExtra.find { it.nombre.split(" ")[0] == "Camarera" }
    }

    @Transactional(readOnly = true)
    fun getPrecio(empresaId: Long, tipoEventoId: Long, fechaEvento: LocalDateTime): Double {
        return getTipoEventoConPrecio(empresaId, tipoEventoId, fechaEvento)?.precio ?: 0.0
    }

    @Transactional(readOnly = true)
    fun getAllPrecioConFecha(empresaId: Long, tipoEventoId: Long): List<PrecioConFechaDTO> {
        return empresaService.getAllPrecioConFechaByTipoEvento(empresaId, tipoEventoId)
    }

    @Transactional(readOnly = true)
    fun getExtrasConPrecio(
        empresaId: Long,
        tipoEventoId: Long,
        fechaEvento: LocalDateTime,
        tipoExtra: TipoExtra
    ): List<ExtraPrecioDTO> {
        return extraService.getAllExtraConPrecioByTipoEventoAndFecha(empresaId, tipoEventoId, fechaEvento, tipoExtra)
    }

    // ==================== MUTATIONS ====================

    @Transactional
    @CacheEvict(value = ["tipos-evento", "tipos-evento-count"], allEntries = true)
    fun saveTipoEvento(dto: TipoEventoDTO): TipoEventoDTO {
        val capacidad = capacidadService.reutilizarCapacidad(dto.capacidad)
        val empresa = empresaService.get(dto.empresaId)
            ?: throw NotFoundException("Empresa no encontrada con id: ${dto.empresaId}")

        val tipoEvento = TipoEvento(
            dto.id,
            dto.nombre,
            dto.duracion,
            capacidad,
            LocalTime.of(dto.cantidadDuracion.hour, dto.cantidadDuracion.minute),
            empresa
        )

        return tipoEventoRepository.save(tipoEvento).toDTO()
    }

    @Transactional
    @CacheEvict(value = ["tipos-evento", "tipos-evento-count"], allEntries = true)
    fun deleteTipoEvento(id: Long): TipoEventoDTO {
        val tipoEvento = tipoEventoRepository.findById(id)
            .orElseThrow { NotFoundException("TipoEvento no encontrado con id: $id") }

        tipoEvento.fechaBaja = LocalDate.now()
        tipoEventoRepository.save(tipoEvento)

        return tipoEvento.toDTO()
    }

    @Transactional
    @CacheEvict(value = ["tipos-evento", "tipos-evento-count"], allEntries = true)
    fun savePreciosConFecha(
        empresaId: Long,
        tipoEventoId: Long,
        listaPrecioConFechaDTO: MutableSet<PrecioConFechaDTO>
    ) {
        val tipoEvento = tipoEventoRepository.findById(tipoEventoId)
            .orElseThrow { NotFoundException("TipoEvento no encontrado con id: $tipoEventoId") }
        val empresa = empresaService.get(empresaId)
            ?: throw NotFoundException("Empresa no encontrada con id: $empresaId")

        // Soft-delete de los precios que ya no están en la lista nueva
        empresa.listaPrecioConFechaTipoEvento
            .filter { it.tipoEvento.id == tipoEvento.id }
            .filter { existing -> listaPrecioConFechaDTO.none { it.id == existing.id } }
            .forEach { precioViejo ->
                precioConFechaTipoEventoService.get(precioViejo.id)?.let {
                    it.fechaBaja = LocalDate.now()
                    precioConFechaTipoEventoService.save(it)
                }
            }

        // Upsert de los precios nuevos
        listaPrecioConFechaDTO.forEach { dto ->
            val fechaHasta = dto.hasta.toEndOfMonth()

            precioConFechaTipoEventoService.save(
                PrecioConFechaTipoEvento(
                    dto.id,
                    dto.precio,
                    dto.desde,
                    fechaHasta,
                    empresa,
                    tipoEvento
                )
            )
        }
    }
}