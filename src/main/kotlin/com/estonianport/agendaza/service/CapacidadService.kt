package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dao.CapacidadDao
import com.estonianport.agendaza.model.Capacidad
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
class CapacidadService : GenericServiceImpl<Capacidad, Long>() {

    @Autowired
    lateinit var capacidadDao: CapacidadDao

    override val dao: CrudRepository<Capacidad, Long>
        get() = capacidadDao

    fun reutilizarCapacidad(capacidad : Capacidad) : Capacidad{
        val listaCapacidad: MutableList<Capacidad>? = this.getAll()

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