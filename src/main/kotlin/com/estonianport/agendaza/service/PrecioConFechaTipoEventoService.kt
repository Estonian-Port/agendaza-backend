package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dao.PrecioConFechaDao
import com.estonianport.agendaza.dao.PrecioConFechaTipoEventoDao
import com.estonianport.agendaza.model.PrecioConFecha
import com.estonianport.agendaza.model.PrecioConFechaTipoEvento
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
class PrecioConFechaTipoEventoService : GenericServiceImpl<PrecioConFechaTipoEvento, Long>(){

    @Autowired
    lateinit var precioConFechaTipoEventoDao: PrecioConFechaTipoEventoDao

    override val dao: CrudRepository<PrecioConFechaTipoEvento, Long>
        get() = precioConFechaTipoEventoDao

}