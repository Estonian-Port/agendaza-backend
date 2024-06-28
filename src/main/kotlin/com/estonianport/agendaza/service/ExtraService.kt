package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.repository.ExtraRepository
import com.estonianport.agendaza.dto.ExtraDTO
import com.estonianport.agendaza.dto.UsuarioAbmDto
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.TipoExtra
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
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


    fun contadorDeExtras(id : Long) = extraRepository.cantidadExtras(id)

    fun extras(id: Long, pageNumber : Int) = extraRepository.findAll(id, PageRequest.of(pageNumber,10)).content
            .map { extra -> extra.toDTO()}

    fun extrasFiltrados(id : Long, pageNumber : Int, buscar: String)=
        extraRepository.extrasByNombre(id, buscar, PageRequest.of(pageNumber,10)).content
            .map { extra -> extra.toDTO()}


    fun contadorDeExtrasCATFiltrados(id : Long,buscar : String) = extraRepository.cantidadExtrasCATFiltrados(id,buscar)

    fun contadorDeExtrasCAT(id : Long) = extraRepository.cantidadExtrasCAT(id)

    fun extrasCAT(id: Long, pageNumber : Int) = extraRepository.findAllCAT(id, PageRequest.of(pageNumber,10)).content
        .map { extra -> extra.toDTO()}

    fun extrasCATFiltrados(id : Long, pageNumber : Int, buscar: String)=
        extraRepository.extrasCATByNombre(id, buscar, PageRequest.of(pageNumber,10)).content
            .map { extra -> extra.toDTO()}


    fun contadorDeExtrasFiltrados(id : Long,buscar : String) = extraRepository.cantidadExtrasFiltrados(id,buscar)


    fun fromListaExtraToListaExtraDto(listaExtra: List<Extra>, fechaEvento : LocalDateTime): List<ExtraDTO>{
        return listaExtra.map{
            it.toExtraPrecioDTO(fechaEvento)
        }
    }

    fun fromListaExtraToListaExtraDtoByFilter(listaExtra: MutableSet<Extra>, fechaEvento : LocalDateTime, tipoExtra : TipoExtra) : List<ExtraDTO>{
        return this.fromListaExtraToListaExtraDto(listaExtra.filter { it.tipoExtra == tipoExtra }, fechaEvento)
    }
}