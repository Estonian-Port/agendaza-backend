package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dao.ExtraVariableDao
import com.estonianport.agendaza.dto.EventoExtraVariableDto
import com.estonianport.agendaza.model.EventoExtraVariable
import com.estonianport.agendaza.model.TipoExtra
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime


@Service
class ExtraVariableService : GenericServiceImpl<EventoExtraVariable, Long>() {

    @Autowired
    lateinit var extraVariableDao: ExtraVariableDao

    @Autowired
    lateinit var extraService: ExtraService

    override val dao: CrudRepository<EventoExtraVariable, Long>
        get() = extraVariableDao

    fun fromListaExtraVariableDtoToListaExtraVariable(listaExtraVariableDto : List<EventoExtraVariableDto>) : List<EventoExtraVariable>{
        return listaExtraVariableDto.map { extraVariable -> EventoExtraVariable(0, extraService.get(extraVariable.id)!!, extraVariable.cantidad) }
    }

    fun fromListaExtraVariableToListaExtraVariableDto(listaEventoExtraVariable: List<EventoExtraVariable>, fechaEvento: LocalDateTime): List<EventoExtraVariableDto>{
        return listaEventoExtraVariable.map { EventoExtraVariableDto(it.extra.id, it.cantidad) }
    }

    fun fromListaExtraVariableToListaExtraVariableDtoByFilter(listaEventoExtraVariable: MutableSet<EventoExtraVariable>, fechaEvento: LocalDateTime, tipoExtra : TipoExtra): List<EventoExtraVariableDto>{
        return fromListaExtraVariableToListaExtraVariableDto(listaEventoExtraVariable.filter { it.extra.tipoExtra == tipoExtra }, fechaEvento)
    }

}
