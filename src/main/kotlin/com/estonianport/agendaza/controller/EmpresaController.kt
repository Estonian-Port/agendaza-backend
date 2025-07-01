package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.*
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.service.EmpresaService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin("*")
class EmpresaController {

    @Autowired
    lateinit var empresaService: EmpresaService

    @GetMapping("/getAllEmpresa")
    fun getAll(): MutableList<Empresa>? {
        return empresaService.getAll()
    }

    @GetMapping("/getEmpresa/{id}")
    fun get(@PathVariable("id") id: Long): Empresa? {
        return empresaService.get(id)
    }

    @PostMapping("/saveEmpresa")
    fun save(@RequestBody empresaDTO: EmpresaDTO): ResponseEntity<GenericItemDTO> {
        return ResponseEntity.ok(empresaService.save(empresaDTO))
    }

    @GetMapping("/getAllEventoByEmpresaId/{id}/{pageNumber}")
    fun getAllEventoByEmpresaId(@PathVariable("id") id: Long, @PathVariable("pageNumber") pageNumber: Int): List<EventoDTO> {
        return empresaService.getAllEventoByEmpresaId(id, pageNumber)
    }

    @GetMapping("/getAllEventoByFilterName/{empresaId}/{pageNumber}/{buscar}")
    fun getAllEventoByFilterName(@PathVariable("empresaId") empresaId: Long, @PathVariable("pageNumber") pageNumber: Int, @PathVariable("buscar") buscar: String): List<EventoDTO> {
        return empresaService.getAllEventoByFilterName(empresaId, pageNumber, buscar)
    }

    @PutMapping("/getAllTipoEventoByEmpresaIdAndDuracion/{id}")
    fun getAllTipoEventoByEmpresaIdAndDuracion(@PathVariable("id") id : Long, @RequestBody duracion : String): List<TipoEventoDTO> {
        return empresaService.get(id)!!.listaTipoEvento.filter {
            it.fechaBaja == null && it.duracion.name == duracion }.map { it.toDTO() }
    }

    @GetMapping("/getEspecificaciones/{id}")
    fun getEspecificaciones(@PathVariable("id") empresaId : Long): List<EspecificacionDTO> {
        return empresaService.getEspecificaciones(empresaId)
    }

}