package com.estonianport.agendaza.controller

import com.estonianport.agendaza.model.Empresa
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
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin("*")
class EmpresaController {

    @Autowired
    lateinit var EmpresaService: EmpresaService

    @GetMapping("/getAllEmpresa")
    fun getAll(): MutableList<Empresa>? {
        return EmpresaService.getAll()
    }

    @GetMapping("/getEmpresa/{id}")
    fun get(@PathVariable("id") id: Long): ResponseEntity<Empresa>? {
        if (id != 0L) {
            return ResponseEntity<Empresa>(EmpresaService.get(id), HttpStatus.OK)
        }
        return ResponseEntity<Empresa>(HttpStatus.NO_CONTENT)
    }

    @PostMapping("/saveEmpresa")
    fun save(@RequestBody Empresa: Empresa): ResponseEntity<Empresa> {
        return ResponseEntity<Empresa>(EmpresaService.save(Empresa), HttpStatus.OK)
    }

    @DeleteMapping("/deleteEmpresa/{id}")
    fun delete(@PathVariable("id") id: Long): ResponseEntity<Empresa> {

        // TODO Elimina todos los eventos del Empresa
        //val listaEventos: List<Evento> = eventoService.getEventosByEmpresa(EmpresaService.get(id))
        //for (evento in listaEventos) {
          //  eventoService.delete(evento.getId())
        //}

        //Elmina el Empresa
        EmpresaService.delete(id)
        return ResponseEntity<Empresa>(HttpStatus.OK)
    }
}