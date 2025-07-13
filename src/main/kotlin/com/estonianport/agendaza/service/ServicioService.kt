package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dto.ServicioDTO
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.repository.ServicioRepository
import com.estonianport.agendaza.model.Servicio
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
class ServicioService : GenericServiceImpl<Servicio, Long>(){

    @Autowired
    private lateinit var empresaService: EmpresaService

    @Autowired
    lateinit var servicioRepository: ServicioRepository

    override val dao: CrudRepository<Servicio, Long>
        get() = servicioRepository

    fun deleteService(servicioId : Long, empresaId: Long){
        val empresa: Empresa = empresaService.get(empresaId)!!

        empresa.listaServicio = empresa.listaServicio.filter { servicio -> servicio.id != servicioId }.toMutableSet()
        empresaService.save(empresa)
    }

    fun getAllServicioByEmpresaId(empresaId: Long, pageNumber: Int): List<ServicioDTO> {
        return servicioRepository.getAllServicioByEmpresaId(empresaId, PageRequest.of(pageNumber,10)).content
    }

    fun getCantidadServicio(empresaId: Long): Int {
        return servicioRepository.getCantidadServicio(empresaId)
    }

    fun getAllServicioFiltradosNombre(empresaId: Long, buscar: String, pageNumber: Int): List<ServicioDTO> {
        return servicioRepository.getAllServicioFilterNombre(empresaId, buscar, PageRequest.of(pageNumber,10)).content
    }

    fun getCantidadServicioFiltrados(empresaId: Long, buscar: String): Int {
        return servicioRepository.getCantidadServicioFiltrados(empresaId, buscar)
    }

    fun getAllServicioAgregar(empresaId: Long): List<ServicioDTO> {
        return servicioRepository.getAllServicioAgregar(empresaId)
    }

    fun getAllServicioByTipoEventoId(id: Long): List<ServicioDTO> {
        return servicioRepository.getAllServicioByTipoEventoId(id)
    }

}