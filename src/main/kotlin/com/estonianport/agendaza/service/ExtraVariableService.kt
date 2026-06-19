package com.estonianport.agendaza.service

import com.estonianport.agendaza.common.GenericServiceImpl
import com.estonianport.agendaza.dto.EventoExtraVariableDTO
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.EventoExtraVariable
import com.estonianport.agendaza.model.enums.TipoExtra
import com.estonianport.agendaza.repository.ExtraVariableRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ExtraVariableService : GenericServiceImpl<EventoExtraVariable, Long>() {

    @Autowired
    lateinit var extraVariableRepository: ExtraVariableRepository

    @Autowired
    lateinit var extraService: ExtraService

    override val dao: CrudRepository<EventoExtraVariable, Long>
        get() = extraVariableRepository

    @Transactional(readOnly = true)
    fun fromListaExtraVariableDtoToListaExtraVariable(
        listaDto: List<EventoExtraVariableDTO>
    ): List<EventoExtraVariable> =
        listaDto.map { EventoExtraVariable(0, extraService.get(it.id)!!, it.cantidad) }

    @Transactional(readOnly = true)
    fun fromListaExtraVariableToListaExtraVariableDto(
        empresa: Empresa,
        lista: List<EventoExtraVariable>,
        fechaEvento: LocalDateTime
    ): List<EventoExtraVariableDTO> =
        lista.map {
            EventoExtraVariableDTO(
                id = it.extra.id,
                cantidad = it.cantidad,
                nombre = it.extra.nombre,
                precio = empresa.getPrecioOfExtraVariableByFecha(it, fechaEvento)
            )
        }

    @Transactional(readOnly = true)
    fun fromListaExtraVariableToListaExtraVariableDtoByFilter(
        empresa: Empresa,
        lista: MutableSet<EventoExtraVariable>,
        fechaEvento: LocalDateTime,
        tipoExtra: TipoExtra
    ): List<EventoExtraVariableDTO> =
        fromListaExtraVariableToListaExtraVariableDto(
            empresa,
            lista.filter { it.extra.tipoExtra == tipoExtra },
            fechaEvento
        )
}