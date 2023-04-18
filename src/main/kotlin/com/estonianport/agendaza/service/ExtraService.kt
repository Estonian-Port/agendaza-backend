package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.repository.ExtraRepository
import com.estonianport.agendaza.dto.ExtraDTO
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

    fun fromListaExtraDtoToListaExtra(listaExtraDTO : List<ExtraDTO>) : List<Extra>{
        return listaExtraDTO.map { extra -> this.get(extra.id)!! }
    }

    fun fromListaExtraToListaExtraDto(listaExtra: List<Extra>, fechaEvento : LocalDateTime): List<ExtraDTO>{
        return listaExtra.map{
            it.toExtraPrecioDTO(fechaEvento)
        }
    }

    fun fromListaExtraToListaExtraDtoByFilter(listaExtra: MutableSet<Extra>, fechaEvento : LocalDateTime, tipoExtra : TipoExtra) : List<ExtraDTO>{
        return this.fromListaExtraToListaExtraDto(listaExtra.filter { it.tipoExtra == tipoExtra }, fechaEvento)
    }
}