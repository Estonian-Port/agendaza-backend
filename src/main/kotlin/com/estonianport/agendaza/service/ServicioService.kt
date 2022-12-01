package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dao.ServicioDao
import com.estonianport.agendaza.model.Servicio
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
class ServicioService : GenericServiceImpl<Servicio, Long>(){

    @Autowired
    lateinit var servicioDao: ServicioDao

    override val dao: CrudRepository<Servicio, Long>
        get() = servicioDao

    fun count(): Long {
        return servicioDao.count()
    }
}