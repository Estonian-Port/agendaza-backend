package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dao.ExtraVariableDao
import com.estonianport.agendaza.dto.ExtraVariableReservaDto
import com.estonianport.agendaza.dto.PagoDto
import com.estonianport.agendaza.model.EventoExtraVariable
import com.estonianport.agendaza.model.Pago
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

    fun fromListaExtraVariableDtoToListaExtraVariable(listaExtraVariableDto : List<ExtraVariableReservaDto>) : List<EventoExtraVariable>{
        return listaExtraVariableDto.map { extraVariable -> fromExtraVariableReservaDtoToEventoExtraVariable(extraVariable) }
    }

    fun fromExtraVariableReservaDtoToEventoExtraVariable(extraVariable : ExtraVariableReservaDto) : EventoExtraVariable{
        return EventoExtraVariable(0, extraService.get(extraVariable.id)!!, extraVariable.cantidad)
    }

    fun getListaExtraVariableReservaDto(listaEventoExtraVariable: MutableSet<EventoExtraVariable>, fechaEvento: LocalDateTime): MutableSet<ExtraVariableReservaDto>{
        val listaExtraDto = mutableSetOf<ExtraVariableReservaDto>()

        listaEventoExtraVariable.forEach{
            val extraDto = ExtraVariableReservaDto(it.extra.id, it.cantidad)

            listaExtraDto.add(extraDto)
        }
        return listaExtraDto
    }

}
