package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dao.PrecioConFechaDao
import com.estonianport.agendaza.model.PrecioConFecha
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
class PrecioConFechaService : GenericServiceImpl<PrecioConFecha, Long>(){

    @Autowired
    lateinit var precioConFechaDao: PrecioConFechaDao

    override val dao: CrudRepository<PrecioConFecha, Long>
        get() = precioConFechaDao

}