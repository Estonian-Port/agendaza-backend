package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dao.PrecioConFechaExtraDao
import com.estonianport.agendaza.model.PrecioConFechaExtra
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
class PrecioConFechaExtraService : GenericServiceImpl<PrecioConFechaExtra, Long>(){

    @Autowired
    lateinit var precioConFechaExtraDao: PrecioConFechaExtraDao

    override val dao: CrudRepository<PrecioConFechaExtra, Long>
        get() = precioConFechaExtraDao

}