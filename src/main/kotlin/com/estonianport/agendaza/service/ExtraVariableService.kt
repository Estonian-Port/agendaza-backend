package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.repository.ExtraVariableRepository
import com.estonianport.agendaza.dto.EventoExtraVariableDTO
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.EventoExtraVariable
import com.estonianport.agendaza.model.enums.TipoExtra
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime


@Service
class ExtraVariableService : GenericServiceImpl<EventoExtraVariable, Long>() {

    @Autowired
    lateinit var extraVariableRepository: ExtraVariableRepository

    @Autowired
    lateinit var extraService: ExtraService

    override val dao: CrudRepository<EventoExtraVariable, Long>
        get() = extraVariableRepository

    fun fromListaExtraVariableDtoToListaExtraVariable(listaExtraVariableDto : List<EventoExtraVariableDTO>) : List<EventoExtraVariable>{
        return listaExtraVariableDto.map { extraVariable -> EventoExtraVariable(0, extraService.get(extraVariable.id)!!, extraVariable.cantidad) }
    }

    fun fromListaExtraVariableToListaExtraVariableDto(empresa : Empresa, listaEventoExtraVariable: List<EventoExtraVariable>, fechaEvento: LocalDateTime): List<EventoExtraVariableDTO>{
        return listaEventoExtraVariable.map { EventoExtraVariableDTO(it.extra.id, it.cantidad, it.extra.nombre, empresa.getPrecioOfExtraVariableByFecha(it, fechaEvento)) }
    }

    fun fromListaExtraVariableToListaExtraVariableDtoByFilter(empresa : Empresa, listaEventoExtraVariable: MutableSet<EventoExtraVariable>, fechaEvento: LocalDateTime, tipoExtra : TipoExtra): List<EventoExtraVariableDTO>{
        return fromListaExtraVariableToListaExtraVariableDto(empresa, listaEventoExtraVariable.filter { it.extra.tipoExtra == tipoExtra }, fechaEvento)
    }

}
