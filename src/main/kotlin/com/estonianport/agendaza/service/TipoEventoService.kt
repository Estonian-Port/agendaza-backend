package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dto.ServicioDTO
import com.estonianport.agendaza.dto.TipoEventoDTO
import com.estonianport.agendaza.dto.TipoEventoPrecioDTO
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.repository.TipoEventoRepository
import com.estonianport.agendaza.model.TipoEvento
import com.estonianport.agendaza.model.enums.TipoExtra
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

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

    fun getAllTipoEventoByEmpresaId(empresaId: Long): List<TipoEventoDTO> {
        return tipoEventoRepository.getAllTipoEventoByEmpresaId(empresaId)
    }

    fun getAllTipoEventoByEmpresaId(empresaId: Long, pageNumber: Int): List<TipoEventoDTO> {
        return tipoEventoRepository.getAllTipoEventoByEmpresaId(empresaId, PageRequest.of(pageNumber,10)).content
    }

    fun getCantidadTipoEvento(empresaId: Long): Int {
        return tipoEventoRepository.getCantidadTipoEvento(empresaId)
    }

    fun getAllTipoEventoFilterNombre(empresaId: Long, buscar: String, pageNumber: Int): List<TipoEventoDTO> {
        return tipoEventoRepository.getAllTipoEventoFilterNombre(empresaId, buscar, PageRequest.of(pageNumber,10)).content
    }

    fun getCantidadTipoEventoFiltrados(empresaId: Long, buscar: String): Int {
        return tipoEventoRepository.getCantidadTipoEventoFiltrados(empresaId, buscar)
    }

    fun getTipoEventoConPrecio(empresaId: Long, tipoEventoId: Long, fechaEvento: LocalDateTime): TipoEventoPrecioDTO?{
        return tipoEventoRepository.getTipoEventoConPrecio(empresaId, tipoEventoId, fechaEvento)
    }

}