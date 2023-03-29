package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dao.ExtraDao
import com.estonianport.agendaza.dto.EventoExtraDto
import com.estonianport.agendaza.dto.ExtraDto
import com.estonianport.agendaza.dto.ExtraVariableDto
import com.estonianport.agendaza.dto.ExtraVariableReservaDto
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.EventoExtraVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ExtraService : GenericServiceImpl<Extra, Long>(){

    @Autowired
    lateinit var extraDao: ExtraDao

    override val dao: CrudRepository<Extra, Long>
        get() = extraDao

    fun getListaExtraDto(listaExtra: MutableSet<Extra>, fechaEvento : LocalDateTime): MutableSet<ExtraDto>{
        val listaExtraDto = mutableSetOf<ExtraDto>()

        listaExtra.forEach{
            val extraDto = ExtraDto(it.id, it.nombre, it.tipoExtra, it.empresa.id)
            if(it.listaPrecioConFecha.isNotEmpty() && it.listaPrecioConFecha.find { it.desde == fechaEvento || it.desde.isBefore(fechaEvento) && it.hasta.isAfter(fechaEvento) } != null ){
                extraDto.precio = it.listaPrecioConFecha.find { it.desde == fechaEvento || it.desde.isBefore(fechaEvento) && it.hasta.isAfter(fechaEvento) }!!.precio
            }else{
                extraDto.precio = 0
            }
            listaExtraDto.add(extraDto)
        }
        return listaExtraDto
    }

    fun getListaExtraVariableDto(listaEventoExtraVariable: MutableSet<EventoExtraVariable>, fechaEvento: LocalDateTime): MutableSet<ExtraVariableDto>{
        val listaExtraDto = mutableSetOf<ExtraVariableDto>()

        listaEventoExtraVariable.forEach{
            val extraDto = ExtraVariableDto(it.extra.id, it.extra.nombre, it.extra.tipoExtra, it.cantidad)
            if(it.extra.listaPrecioConFecha.isNotEmpty() && it.extra.listaPrecioConFecha.find { it.desde == fechaEvento || it.desde.isBefore(fechaEvento) && it.hasta.isAfter(fechaEvento) } != null ){
                extraDto.precio = it.extra.listaPrecioConFecha.find { it.desde == fechaEvento || it.desde.isBefore(fechaEvento) && it.hasta.isAfter(fechaEvento) }!!.precio
            }else{
                extraDto.precio = 0
            }
            listaExtraDto.add(extraDto)
        }
        return listaExtraDto
    }

    fun fromListaExtraDtoToListaExtra(listaExtraDto : List<ExtraDto>) : List<Extra>{
        return listaExtraDto.map { extra -> this.get(extra.id)!! }
    }
}