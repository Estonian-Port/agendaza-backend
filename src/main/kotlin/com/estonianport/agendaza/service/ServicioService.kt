package com.estonianport.agendaza.service

import com.estonianport.agendaza.common.GenericServiceImpl
import com.estonianport.agendaza.common.limpiarNombre
import com.estonianport.agendaza.dto.GenericItemDTO
import com.estonianport.agendaza.dto.ServicioDTO
import com.estonianport.agendaza.dto.toDTO
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.Servicio
import com.estonianport.agendaza.repository.ServicioRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ServicioService(
    private val servicioRepository: ServicioRepository,
    private val empresaService: EmpresaService,
    private val tipoEventoService: TipoEventoService
) : GenericServiceImpl<Servicio, Long>() {

    override val dao: CrudRepository<Servicio, Long>
        get() = servicioRepository

    // ==================== QUERIES ====================

    @Transactional(readOnly = true)
    @Cacheable(value = ["servicios-detalle"], key = "#id + '-' + #empresaId")
    fun getServicioConTiposEvento(id: Long, empresaId: Long): ServicioDTO {
        val servicio = servicioRepository.findById(id)
            .orElseThrow { NotFoundException("Servicio no encontrado con id: $id") }
        return servicio.toDTO().also {
            it.listaTipoEventoId = servicioRepository.getTipoEventoIdsByServicioAndEmpresa(id, empresaId)
        }
    }

    @Cacheable(value = ["servicios-page"], key = "#empresaId + '-' + #page + '-' + #buscar")
    fun getPage(empresaId: Long, page: Int, buscar: String?): List<ServicioDTO> =
        servicioRepository.getPage(empresaId, buscar?.limpiarNombre()?.ifBlank { null }, PageRequest.of(page, 10))

    @Cacheable(value = ["servicios-count"], key = "#empresaId + '-' + #buscar")
    fun count(empresaId: Long, buscar: String?): Int =
        servicioRepository.count(empresaId, buscar?.limpiarNombre()?.ifBlank { null })

    @Cacheable(value = ["servicios-buscar"], key = "#empresaId + '-' + #q")
    fun buscarParaAgregar(empresaId: Long, q: String): List<ServicioDTO> =
        servicioRepository.buscarParaAgregar(empresaId, q.limpiarNombre(), PageRequest.of(0, 10))

    @Cacheable(value = ["servicios-tipo-evento"], key = "#tipoEventoId")
    fun getAllServicioByTipoEventoId(tipoEventoId: Long): List<ServicioDTO> =
        servicioRepository.getAllServicioByTipoEventoId(tipoEventoId)

    // ==================== MUTATIONS ====================

    @Transactional
    @CacheEvict(
        value = ["servicios-detalle", "servicios-page", "servicios-count", "servicios-buscar", "servicios-tipo-evento", "panelAdminCantidades"],
        allEntries = true
    )
    fun saveServicio(dto: GenericItemDTO): ServicioDTO {
        val empresa = empresaService.findById(dto.empresaId)
        val nombre = dto.nombre.limpiarNombre()

        val servicio = if (dto.id != 0L) {
            servicioRepository.findById(dto.id)
                .orElseThrow { NotFoundException("Servicio no encontrado con id: ${dto.id}") }
                .also {
                    // Si lo comparten otras empresas no se renombra
                    if (servicioRepository.countOtrasEmpresas(it.id, empresa.id) == 0) it.nombre = nombre
                }
        } else {
            servicioRepository.findFirstByNombreIgnoreCaseOrderByIdAsc(nombre) ?: Servicio(0, nombre)
        }

        val nuevos = if (dto.listaTipoEventoId.isEmpty()) emptyList()
        else tipoEventoService.getAllByIdsAndEmpresa(dto.listaTipoEventoId, empresa.id)

        // Solo toca los tipos de evento de esta empresa
        servicio.listaTipoEvento.removeIf { it.empresa.id == empresa.id }
        servicio.listaTipoEvento.addAll(nuevos)

        val saved = servicioRepository.save(servicio)
        if (empresa.listaServicio.add(saved)) empresaService.save(empresa)

        return saved.toDTO()
    }

    @Transactional
    @CacheEvict(
        value = ["servicios-detalle", "servicios-page", "servicios-count", "servicios-buscar", "servicios-tipo-evento", "panelAdminCantidades"],
        allEntries = true
    )
    fun deleteService(servicioId: Long, empresaId: Long) {
        val empresa = empresaService.get(empresaId)
            ?: throw NotFoundException("Empresa no encontrada con id: $empresaId")

        empresa.listaServicio.removeIf { it.id == servicioId }
        empresaService.save(empresa)

        // Limpia los vínculos con los tipos de evento de esta empresa
        servicioRepository.findById(servicioId).ifPresent { s ->
            s.listaTipoEvento.removeIf { it.empresa.id == empresaId }
        }
    }
}
