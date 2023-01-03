package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dao.EventoDao
import com.estonianport.agendaza.model.Evento
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
class EventoService : GenericServiceImpl<Evento, Long>() {

    @Autowired
    lateinit var eventoDao: EventoDao

    override val dao: CrudRepository<Evento, Long>
        get() = eventoDao
}