package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dao.CapacidadDao
import com.estonianport.agendaza.model.Capacidad
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
open class CapacidadService : GenericServiceImpl<Capacidad, Long>() {

    @Autowired
    lateinit var capacidadDao: CapacidadDao

    override val dao: CrudRepository<Capacidad, Long>
        get() = capacidadDao
}