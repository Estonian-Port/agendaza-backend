package com.estonianport.agendaza.service

import com.estonianport.agendaza.common.GenericServiceImpl
import com.estonianport.agendaza.repository.CapacidadRepository
import com.estonianport.agendaza.model.Capacidad
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
class CapacidadService : GenericServiceImpl<Capacidad, Long>() {

    @Autowired
    lateinit var capacidadRepository: CapacidadRepository

    override val dao: CrudRepository<Capacidad, Long>
        get() = capacidadRepository

    fun reutilizarCapacidad(capacidad: Capacidad): Capacidad {
        return capacidadRepository.findByCapacidadAdultosAndCapacidadNinos(
            capacidad.capacidadAdultos,
            capacidad.capacidadNinos
        ) ?: capacidad
    }

}