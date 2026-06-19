package com.estonianport.agendaza.service

import com.estonianport.agendaza.common.GenericServiceImpl
import com.estonianport.agendaza.common.toEndOfMonth
import com.estonianport.agendaza.dto.ExtraDTO
import com.estonianport.agendaza.dto.ExtraPrecioDTO
import com.estonianport.agendaza.dto.PrecioConFechaDTO
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.PrecioConFechaExtra
import com.estonianport.agendaza.model.enums.TipoExtra
import com.estonianport.agendaza.repository.ExtraRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class ExtraService : GenericServiceImpl<Extra, Long>() {

    @Autowired
    private lateinit var empresaService: EmpresaService

    @Autowired
    lateinit var extraRepository: ExtraRepository

    @Autowired
    lateinit var precioConFechaExtraService: PrecioConFechaExtraService

    override val dao: CrudRepository<Extra, Long>
        get() = extraRepository

    // ==================== CONVERSIÓN ====================

    fun fromListaExtraDtoToListaExtra(listaExtraDTO: List<ExtraDTO>): List<Extra> =
        listaExtraDTO.map { get(it.id)!! }

    fun fromListaExtraToListaExtraDto(
        empresa: Empresa,
        listaExtra: List<Extra>,
        fechaEvento: LocalDateTime
    ): List<ExtraDTO> = listaExtra.map { it.toExtraPrecioDTO(empresa, fechaEvento) }

    fun fromListaExtraToListaExtraDtoByFilter(
        empresa: Empresa,
        listaExtra: MutableSet<Extra>,
        fechaEvento: LocalDateTime,
        tipoExtra: TipoExtra
    ): List<ExtraDTO> = fromListaExtraToListaExtraDto(
        empresa,
        listaExtra.filter { it.tipoExtra == tipoExtra },
        fechaEvento
    )

    // ==================== EVENTO - PAGINADO ====================

    @Transactional(readOnly = true)
    fun countEvento(empresaId: Long): Int =
        extraRepository.countEvento(empresaId)

    @Transactional(readOnly = true)
    fun countEventoByNombre(empresaId: Long, buscar: String): Int =
        extraRepository.countEventoByNombre(empresaId, buscar)

    @Transactional(readOnly = true)
    fun getPageEvento(empresaId: Long, pageNumber: Int): List<ExtraDTO> =
        extraRepository.findAllEvento(empresaId, PageRequest.of(pageNumber, 10))
            .content.map { it.toDTO() }

    @Transactional(readOnly = true)
    fun getPageEventoByNombre(empresaId: Long, pageNumber: Int, buscar: String): List<ExtraDTO> =
        extraRepository.findAllEventoByNombre(empresaId, buscar, PageRequest.of(pageNumber, 10))
            .content.map { it.toDTO() }

    // ==================== CATERING - PAGINADO ====================

    @Transactional(readOnly = true)
    fun countCatering(empresaId: Long): Int =
        extraRepository.countCatering(empresaId)

    @Transactional(readOnly = true)
    fun countCateringByNombre(empresaId: Long, buscar: String): Int =
        extraRepository.countCateringByNombre(empresaId, buscar)

    @Transactional(readOnly = true)
    fun getPageCatering(empresaId: Long, pageNumber: Int): List<ExtraDTO> =
        extraRepository.findAllCatering(empresaId, PageRequest.of(pageNumber, 10))
            .content.map { it.toDTO() }

    @Transactional(readOnly = true)
    fun getPageCateringByNombre(empresaId: Long, pageNumber: Int, buscar: String): List<ExtraDTO> =
        extraRepository.findAllCateringByNombre(empresaId, buscar, PageRequest.of(pageNumber, 10))
            .content.map { it.toDTO() }

    // ==================== LISTAS GLOBALES (cacheables) ====================

    @Cacheable("extras-evento")
    @Transactional(readOnly = true)
    fun getAllEvento(): List<ExtraDTO> =
        extraRepository.getAllEvento()

    @Cacheable("extras-catering")
    @Transactional(readOnly = true)
    fun getAllCatering(): List<ExtraDTO> =
        extraRepository.getAllCatering()

    @Transactional(readOnly = true)
    fun getAllExtraEventoAgregar(empresaId: Long): List<ExtraDTO> =
        extraRepository.getAllExtraEventoAgregar(empresaId)

    @Transactional(readOnly = true)
    fun getAllExtraCateringAgregar(empresaId: Long): List<ExtraDTO> =
        extraRepository.getAllExtraCateringAgregar(empresaId)

    // ==================== PRECIO ====================

    @Transactional(readOnly = true)
    fun getAllExtraConPrecioByTipoEventoAndFecha(
        empresaId: Long,
        tipoEventoId: Long,
        fechaEvento: LocalDateTime,
        tipoExtra: TipoExtra
    ): List<ExtraPrecioDTO> =
        extraRepository.getAllExtraConPrecioByTipoEventoAndFecha(empresaId, tipoEventoId, fechaEvento, tipoExtra)

    // ==================== ABM ====================

    @Transactional
    @CacheEvict(value = ["extras-evento", "extras-catering"], allEntries = true)
    fun saveExtra(extraDTO: ExtraDTO, tipoEventoService: TipoEventoService): ExtraDTO {
        val extra = if (extraDTO.id != 0L) {
            get(extraDTO.id)!!.apply {
                nombre = extraDTO.nombre
                tipoExtra = extraDTO.tipoExtra
            }
        } else {
            Extra(0, extraDTO.nombre, extraDTO.tipoExtra)
        }

        extra.listaTipoEvento = extraDTO.listaTipoEventoId
            .map { tipoEventoService.get(it)!! }
            .toMutableSet()

        val saved = save(extra)

        val empresa = empresaService.get(extraDTO.empresaId)!!
        empresa.listaExtra.add(saved)
        empresaService.save(empresa)

        return saved.toDTO()
    }

    @Transactional
    @CacheEvict(value = ["extras-evento", "extras-catering"], allEntries = true)
    fun deleteExtra(extraId: Long, empresaId: Long) {
        val empresa = empresaService.get(empresaId)!!
        empresa.listaExtra = empresa.listaExtra
            .filter { it.id != extraId }
            .toMutableSet()
        empresaService.save(empresa)
    }

    @Transactional
    @CacheEvict(value = ["extras-evento", "extras-catering"], allEntries = true)
    fun savePreciosConFecha(
        empresaId: Long,
        extraId: Long,
        listaPrecioConFechaDTO: MutableSet<PrecioConFechaDTO>
    ) {
        val extra = get(extraId)
            ?: throw RuntimeException("Extra no encontrado con id: $extraId")
        val empresa = empresaService.get(empresaId)
            ?: throw RuntimeException("Empresa no encontrada con id: $empresaId")

        // Soft-delete de los precios que ya no están en la lista nueva
        empresa.listaPrecioConFechaExtra
            .filter { it.extra.id == extra.id }
            .filter { existing -> listaPrecioConFechaDTO.none { it.id == existing.id } }
            .forEach { precioViejo ->
                precioConFechaExtraService.get(precioViejo.id)?.let {
                    it.fechaBaja = LocalDate.now()
                    precioConFechaExtraService.save(it)
                }
            }

        // Upsert de los precios nuevos
        listaPrecioConFechaDTO.forEach { dto ->
            val fechaHasta = dto.hasta.toEndOfMonth()

            precioConFechaExtraService.save(
                PrecioConFechaExtra(
                    id = dto.id,
                    precio = dto.precio,
                    desde = dto.desde,
                    hasta = fechaHasta,
                    empresa = empresa,
                    extra = extra
                )
            )
        }
    }
}