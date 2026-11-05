package com.estonianport.agendaza.service

import com.estonianport.agendaza.common.GenericServiceImpl
import com.estonianport.agendaza.common.toEndOfMonth
import com.estonianport.agendaza.dto.ExtraDTO
import com.estonianport.agendaza.dto.ExtraPrecioDTO
import com.estonianport.agendaza.dto.PrecioConFechaDTO
import com.estonianport.agendaza.dto.toDTO
import com.estonianport.agendaza.dto.toExtraPrecioDTO
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.EventoExtra
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
class ExtraService (
    val empresaService: EmpresaService,
    val extraRepository: ExtraRepository,
    val precioConFechaExtraService: PrecioConFechaExtraService
): GenericServiceImpl<Extra, Long>() {

    override val dao: CrudRepository<Extra, Long>
        get() = extraRepository

    // ==================== CONVERSIÓN ====================

    fun fromListaExtraDtoToListaExtra(listaExtraDTO: List<ExtraDTO>, evento: com.estonianport.agendaza.model.Evento): List<EventoExtra> =
        listaExtraDTO.map { dto ->
            val extra = get(dto.id) ?: throw com.estonianport.agendaza.errors.NotFoundException("Extra no encontrado")
            val cantidad = when (extra.tipoExtra) {
                TipoExtra.EVENTO, TipoExtra.TIPO_CATERING -> 1
                TipoExtra.VARIABLE_EVENTO, TipoExtra.VARIABLE_CATERING -> dto.cantidad.coerceAtLeast(1)
            }
            EventoExtra(
                id = 0,
                evento = evento,
                extra = extra,
                precio = dto.precio,
                cantidad = cantidad
            )
        }


    fun aplicarPreciosFoto(listaExtra: Collection<EventoExtra>, tipoExtras: Set<TipoExtra>): List<ExtraDTO> =
        listaExtra.filter { it.extra.tipoExtra in tipoExtras }
            .map { item -> item.extra.toDTO().also { dto -> dto.precio = item.precio; dto.cantidad = item.cantidad } }

    // ====================  PAGINADO ====================

    fun getPage(empresaId: Long, tiposExtra: List<TipoExtra>, page: Int, buscar: String?): List<ExtraDTO> {
        return extraRepository.getPage(empresaId, tiposExtra, buscar, PageRequest.of(page, 10))
    }

    fun count(empresaId: Long, tiposExtra: List<TipoExtra>, buscar: String?): Int {
        return extraRepository.count(empresaId, tiposExtra, buscar)
    }

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
        val empresa = empresaService.get(extraDTO.empresaId)!!

        val extra = if (extraDTO.id != 0L) {
            get(extraDTO.id)!!.also {
                if (extraRepository.countOtrasEmpresas(it.id, empresa.id) == 0) {
                    it.nombre = extraDTO.nombre
                    it.tipoExtra = extraDTO.tipoExtra
                }
            }
        } else {
            Extra(0, extraDTO.nombre, extraDTO.tipoExtra)
        }

        val nuevos = if (extraDTO.listaTipoEventoId.isEmpty()) emptyList()
        else tipoEventoService.getAllByIdsAndEmpresa(extraDTO.listaTipoEventoId, empresa.id)

        extra.listaTipoEvento.removeIf { it.empresa.id == empresa.id }
        extra.listaTipoEvento.addAll(nuevos)

        val saved = save(extra)

        if (empresa.listaExtra.add(saved)) empresaService.save(empresa)

        return saved.toDTO()
    }


    @Transactional
    @CacheEvict(value = ["extras-evento", "extras-catering"], allEntries = true)
    fun deleteExtra(extraId: Long, empresaId: Long) {
        val empresa = empresaService.get(empresaId)!!
        empresa.listaExtra = empresa.listaExtra
            .filter { it.id != extraId }
            .toMutableSet()

        // Al desvincular el extra, sus precios dejan de ser válidos para esta empresa.
        empresa.listaPrecioConFechaExtra
            .filter { it.extra.id == extraId && it.fechaBaja == null }
            .forEach {
                it.fechaBaja = LocalDate.now()
                precioConFechaExtraService.save(it)
            }

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
