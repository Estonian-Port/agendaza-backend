package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dto.ServicioDTO
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.Pago
import com.estonianport.agendaza.repository.ServicioRepository
import com.estonianport.agendaza.model.Servicio
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ServicioService : GenericServiceImpl<Servicio, Long>(){

    @Autowired
    lateinit var servicioRepository: ServicioRepository

    override val dao: CrudRepository<Servicio, Long>
        get() = servicioRepository

    fun fromListaServicioToListaServicioDto(listaServicio: List<Servicio>): List<ServicioDTO> {
        return listaServicio.map{
            it.toDTO()
        }
    }

    fun deleteService(id : Long){
        val servicio = servicioRepository.findById(id)
                .orElseThrow { NotFoundException("Servicio no encontrado") }
        servicio.fechaBaja = LocalDate.now()
        save(servicio)
    }

    fun getAllServicioByEmpresaId(empresaId: Long, pageNumber: Int): List<ServicioDTO> {
        return servicioRepository.getAllServicioByEmpresaId(empresaId, PageRequest.of(pageNumber,10)).content
    }

    fun getCantidadServicio(empresaId: Long): Int {
        return servicioRepository.getCantidadServicio(empresaId)
    }

    fun getAllServicioFilterNombre(empresaId: Long, buscar: String, pageNumber: Int): List<ServicioDTO> {
        return servicioRepository.getAllServicioFilterNombre(empresaId, buscar, PageRequest.of(pageNumber,10)).content
    }

    fun getCantidadServicioFiltrados(empresaId: Long, buscar: String): Int {
        return servicioRepository.getCantidadServicioFiltrados(empresaId, buscar)
    }

}