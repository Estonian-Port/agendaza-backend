package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dto.TipoEventoDTO
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.Servicio
import com.estonianport.agendaza.repository.TipoEventoRepository
import com.estonianport.agendaza.model.TipoEvento
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
class TipoEventoService : GenericServiceImpl<TipoEvento, Long>() {

    @Autowired
    lateinit var tipoEventoRepository: TipoEventoRepository

    override val dao: CrudRepository<TipoEvento, Long>
        get() = tipoEventoRepository

    fun getAllByExtra(extraId : Long): MutableList<TipoEventoDTO>{
        return tipoEventoRepository.getAllByExtra(extraId)
    }

    fun getAllByServicio(servicioId: Long): MutableList<TipoEventoDTO> {
        return tipoEventoRepository.getAllByServicio(servicioId)
    }

    fun listaTipoEventoToListaTipoEventoDTO(listaTipoEvento: MutableList<TipoEvento>): List<TipoEventoDTO> {
        return listaTipoEvento.map { it.toDTO() }
    }


}