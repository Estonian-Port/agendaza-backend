package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.repository.PrecioConFechaTipoEventoRepository
import com.estonianport.agendaza.model.PrecioConFechaTipoEvento
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
class PrecioConFechaTipoEventoService : GenericServiceImpl<PrecioConFechaTipoEvento, Long>(){

    @Autowired
    lateinit var precioConFechaTipoEventoRepository: PrecioConFechaTipoEventoRepository

    override val dao: CrudRepository<PrecioConFechaTipoEvento, Long>
        get() = precioConFechaTipoEventoRepository

}