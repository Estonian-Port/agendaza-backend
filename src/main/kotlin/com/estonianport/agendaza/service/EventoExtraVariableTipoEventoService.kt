package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dao.EventoExtraVariableTipoEventoDao
import com.estonianport.agendaza.model.EventoExtraVariableTipoEvento
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service


@Service
class EventoExtraVariableTipoEventoService : GenericServiceImpl<EventoExtraVariableTipoEvento, Long>() {

    @Autowired
    lateinit var eventoExtraVariableTipoEventoDao: EventoExtraVariableTipoEventoDao

    override val dao: CrudRepository<EventoExtraVariableTipoEvento, Long>
        get() = eventoExtraVariableTipoEventoDao

}
