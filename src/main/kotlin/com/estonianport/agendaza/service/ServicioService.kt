package com.estonianport.agendaza.service

import com.estonianport.agendaza.common.GenericServiceImpl
import com.estonianport.agendaza.dto.GenericItemDTO
import com.estonianport.agendaza.dto.ServicioDTO
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
    fun getServicioConTiposEvento(id: Long): ServicioDTO {
        val servicio = servicioRepository.findById(id)
            .orElseThrow { NotFoundException("Servicio no encontrado con id: $id") }
        return servicio.toDTO().also { dto ->
            dto.listaTipoEventoId = tipoEventoService.getAllByServicio(dto.id).map { it.id }
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["servicios"], key = "#empresaId + '-' + #pageNumber")
    fun getAllServicioByEmpresaId(empresaId: Long, pageNumber: Int): List<ServicioDTO> {
        return servicioRepository
            .getAllServicioByEmpresaId(empresaId, PageRequest.of(pageNumber, 10))
            .content
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["servicios-count"], key = "#empresaId")
    fun getCantidadServicio(empresaId: Long): Int {
        return servicioRepository.getCantidadServicio(empresaId)
    }

    @Transactional(readOnly = true)
    fun getAllServicioFiltradosNombre(empresaId: Long, buscar: String, pageNumber: Int): List<ServicioDTO> {
        return servicioRepository
            .getAllServicioFilterNombre(empresaId, buscar, PageRequest.of(pageNumber, 10))
            .content
    }

    @Transactional(readOnly = true)
    fun getCantidadServicioFiltrados(empresaId: Long, buscar: String): Int {
        return servicioRepository.getCantidadServicioFiltrados(empresaId, buscar)
    }

    @Transactional(readOnly = true)
    fun getAllServicioAgregar(empresaId: Long): List<ServicioDTO> {
        return servicioRepository.getAllServicioAgregar(empresaId)
    }

    @Transactional(readOnly = true)
    fun getAllServicioByTipoEventoId(tipoEventoId: Long): List<ServicioDTO> {
        return servicioRepository.getAllServicioByTipoEventoId(tipoEventoId)
    }

    // ==================== MUTATIONS ====================

    @Transactional
    @CacheEvict(value = ["servicios", "servicios-count"], allEntries = true)
    fun saveServicio(dto: GenericItemDTO): ServicioDTO {
        val servicio = if (dto.id != 0L) {
            servicioRepository.findById(dto.id)
                .orElseThrow { NotFoundException("Servicio no encontrado con id: ${dto.id}") }
                .apply { nombre = dto.nombre }
        } else {
            Servicio(0, dto.nombre)
        }

        servicio.listaTipoEvento = dto.listaTipoEventoId
            .map { tipoEventoService.get(it) ?: throw NotFoundException("TipoEvento no encontrado con id: $it") }
            .toMutableSet()

        val savedServicio = servicioRepository.save(servicio)

        val empresa = empresaService.findById(dto.empresaId)
        if (!empresa.listaServicio.any { it.id == savedServicio.id }) {
            empresa.listaServicio.add(savedServicio)
            empresaService.save(empresa)
        }

        return savedServicio.toDTO()
    }

    @Transactional
    @CacheEvict(value = ["servicios", "servicios-count"], allEntries = true)
    fun deleteService(servicioId: Long, empresaId: Long) {
        val empresa = empresaService.get(empresaId)
            ?: throw NotFoundException("Empresa no encontrada con id: $empresaId")

        empresa.listaServicio = empresa.listaServicio
            .filter { it.id != servicioId }
            .toMutableSet()

        empresaService.save(empresa)
    }
}