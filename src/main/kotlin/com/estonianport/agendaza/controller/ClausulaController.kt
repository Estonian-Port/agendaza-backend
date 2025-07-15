package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.GenericItemDTO
import com.estonianport.agendaza.model.Clausula
import com.estonianport.agendaza.model.Servicio
import com.estonianport.agendaza.service.ClausulaService
import com.estonianport.agendaza.service.EmpresaService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/clausula")
@CrossOrigin("*")
class ClausulaController {

    @Autowired
    lateinit var clausulaService: ClausulaService

    @Autowired
    lateinit var empresaService: EmpresaService

    @GetMapping("/get/{id}")
    fun get(@PathVariable("id") id: Long): GenericItemDTO {
        return clausulaService.get(id)!!.toDTO()
    }

    @PostMapping("/save")
    fun save(@RequestBody clausulaDTO: GenericItemDTO): GenericItemDTO {

        var clausula : Clausula = if(clausulaDTO.id != 0L){
            clausulaService.get(clausulaDTO.id)!!
        }else{
            Clausula(0, clausulaDTO.nombre)
        }

        clausula = clausulaService.save(clausula)

        val empresa = empresaService.findById(clausulaDTO.empresaId)
        empresa.listaClausula.add(clausula)

        empresaService.save(empresa)

        return clausula.toDTO()
    }

    @DeleteMapping("/delete/{clausulaId}/{empresaId}")
    fun delete(@PathVariable("clausulaId") clausulaId: Long, @PathVariable("empresaId") empresaId: Long): ResponseEntity<Clausula> {
        clausulaService.delete(clausulaId, empresaId)
        return ResponseEntity<Clausula>(HttpStatus.OK)
    }

    @GetMapping("/getAll/{empresaId}/{pageNumber}")
    fun getAll(@PathVariable("empresaId") empresaId: Long, @PathVariable("pageNumber") pageNumber : Int): List<GenericItemDTO> {
        return clausulaService.getAll(empresaId, pageNumber)
    }

    @GetMapping("/getAllCantidad/{empresaId}")
    fun getAllCantidad(@PathVariable("empresaId") empresaId: Long): Int {
        return clausulaService.getAllCantidad(empresaId)
    }

    @GetMapping("/getAllFiltro/{empresaId}/{pageNumber}/{buscar}")
    fun getAllFiltro(@PathVariable("empresaId") empresaId: Long, @PathVariable("pageNumber") pageNumber : Int, @PathVariable("buscar") buscar: String): List<GenericItemDTO> {
        return clausulaService.getAllFiltro(empresaId, buscar, pageNumber)
    }

    @GetMapping("/getAllCantidadFiltro/{empresaId}/{buscar}")
    fun getAllCantidadFiltro(@PathVariable("empresaId") empresaId: Long, @PathVariable("buscar") buscar: String): Int {
        return clausulaService.getAllCantidadFiltro(empresaId, buscar)
    }

    @GetMapping("/getAllAgregar/{empresaId}")
    fun getAllAgregar(@PathVariable("empresaId") empresaId: Long): List<GenericItemDTO> {
        return clausulaService.getAllAgregar(empresaId)
    }

}