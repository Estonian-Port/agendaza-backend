package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dto.*
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.repository.EmpresaRepository
import com.estonianport.agendaza.repository.EventoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.CrudRepository
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class EmpresaService : GenericServiceImpl<Empresa, Long>() {

    @Autowired
    lateinit var empresaRepository: EmpresaRepository

    @Autowired
    lateinit var eventoRepository: EventoRepository

    override val dao: CrudRepository<Empresa, Long>
        get() = empresaRepository

    fun findById(id : Long): Empresa {
        return empresaRepository.findById(id).get()
    }

    fun save(empresaDTO: EmpresaDTO): GenericItemDTO{
        val empresa = empresaRepository.findById(empresaDTO.id).orElseThrow {
            NotFoundException("Empresa con ID " + empresaDTO.id + "no encontrada.")
        }

        val empresaActualizada = empresa.copy(empresaDTO.nombre, empresaDTO.telefono, empresaDTO.email,
                empresaDTO.calle, empresaDTO.numero, empresaDTO.municipio)

        return empresaRepository.save(empresaActualizada).toGenericItemDTO()
    }


    fun getEmpresaListaPagoById(id : Long): List<PagoDTO>{
        return empresaRepository.getEmpresaListaPagoById(id)
    }

    fun getAllEventoByEmpresaId(id: Long, pageNumber : Int): List<EventoDTO> {
        return eventoRepository.eventosByEmpresa(id, PageRequest.of(pageNumber,10))
                .map { evento -> evento.toDto() }.toList()
    }
    fun getAllEventoByFilterName(id : Long, pageNumber : Int, buscar: String): List<EventoDTO>{
        return eventoRepository.eventosByNombre(id, buscar, PageRequest.of(pageNumber,10))
            .map { evento -> evento.toDto() }.toList()
    }

    fun getAllCantidadesForPanelAdminByEmpresaId(id: Long): CantidadesPanelAdmin {
        return empresaRepository.getAllCantidadesForPanelAdminByEmpresaId(id)
    }


}