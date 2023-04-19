package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dto.ServicioDTO
import com.estonianport.agendaza.repository.ServicioRepository
import com.estonianport.agendaza.model.Servicio
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

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

}