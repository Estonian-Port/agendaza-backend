package com.estonianport.agendaza.service

import GenericServiceImpl
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

    fun reutilizarCapacidad(capacidad : Capacidad) : Capacidad{
        val listaCapacidad: MutableList<Capacidad>? = this.getAll()

        // TODO refactor
        // Reutilizar capacidades ya guardadas
        if (listaCapacidad != null && listaCapacidad.size != 0) {
            for (capacidadDDBB in listaCapacidad) {
                if (capacidadDDBB.capacidadAdultos == capacidad.capacidadAdultos
                    && capacidadDDBB.capacidadNinos == capacidad.capacidadNinos) {
                    return capacidadDDBB
                }
            }
        }
        return capacidad
    }

}