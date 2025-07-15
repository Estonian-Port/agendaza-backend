package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dto.GenericItemDTO
import com.estonianport.agendaza.dto.ServicioDTO
import com.estonianport.agendaza.model.Clausula
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Servicio
import com.estonianport.agendaza.repository.ClausulaRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
class ClausulaService : GenericServiceImpl<Clausula, Long>(){

    @Autowired
    private lateinit var empresaService: EmpresaService

    @Autowired
    lateinit var clausulaRepository: ClausulaRepository

    override val dao: CrudRepository<Clausula, Long>
        get() = clausulaRepository


    fun delete(clausulaId : Long, empresaId: Long){
        val empresa: Empresa = empresaService.get(empresaId)!!

        empresa.listaClausula = empresa.listaClausula.filter { clausula -> clausula.id != clausulaId }.toMutableSet()
        empresaService.save(empresa)
    }

    fun getAll(empresaId: Long, pageNumber: Int): List<GenericItemDTO> {
        return clausulaRepository.getAll(empresaId, PageRequest.of(pageNumber,10)).content
    }

    fun getAllCantidad(empresaId: Long): Int {
        return clausulaRepository.getAllCantidad(empresaId)
    }

    fun getAllFiltro(empresaId: Long, buscar: String, pageNumber: Int): List<GenericItemDTO> {
        return clausulaRepository.getAllFiltro(empresaId, buscar, PageRequest.of(pageNumber,10)).content
    }

    fun getAllCantidadFiltro(empresaId: Long, buscar: String): Int {
        return clausulaRepository.getAllCantidadFiltro(empresaId, buscar)
    }

    fun getAllAgregar(empresaId: Long): List<GenericItemDTO> {
        return clausulaRepository.getAllAgregar(empresaId)
    }

}