package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.repository.ExtraRepository
import com.estonianport.agendaza.dto.ExtraDto
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.TipoExtra
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ExtraService : GenericServiceImpl<Extra, Long>(){

    @Autowired
    lateinit var extraRepository: ExtraRepository

    override val dao: CrudRepository<Extra, Long>
        get() = extraRepository

    fun fromListaExtraDtoToListaExtra(listaExtraDto : List<ExtraDto>) : List<Extra>{
        return listaExtraDto.map { extra -> this.get(extra.id)!! }
    }

    fun fromListaExtraToListaExtraDto(listaExtra: List<Extra>, fechaEvento : LocalDateTime): List<ExtraDto>{
        return listaExtra.map{
            ExtraDto(it.id, it.nombre, it.tipoExtra, it.empresa.id, it.empresa.getPrecioOfExtraByFecha(it, fechaEvento))
        }
    }

    fun fromListaExtraToListaExtraDtoByFilter(listaExtra: MutableSet<Extra>, fechaEvento : LocalDateTime, tipoExtra : TipoExtra) : List<ExtraDto>{
        return this.fromListaExtraToListaExtraDto(listaExtra.filter { it.tipoExtra == tipoExtra }, fechaEvento)
    }
}