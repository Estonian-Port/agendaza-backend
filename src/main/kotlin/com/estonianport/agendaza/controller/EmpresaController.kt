package com.estonianport.agendaza.controller

import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.Servicio
import com.estonianport.agendaza.model.TipoEvento
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
    lateinit var empresaService: EmpresaService

    @GetMapping("/getAllEmpresa")
    fun getAll(): MutableList<Empresa>? {
        return empresaService.getAll()
    }

    @GetMapping("/getEmpresa/{id}")
    fun get(@PathVariable("id") id: Long): ResponseEntity<Empresa>? {
        if (id != 0L) {
            return ResponseEntity<Empresa>(empresaService.get(id), HttpStatus.OK)
        }
        return ResponseEntity<Empresa>(HttpStatus.NO_CONTENT)
    }

    @PostMapping("/saveEmpresa")
    fun save(@RequestBody empresa: Empresa): ResponseEntity<Empresa> {
        return ResponseEntity<Empresa>(empresaService.save(empresa), HttpStatus.OK)
    }

    @DeleteMapping("/deleteEmpresa/{id}")
    fun delete(@PathVariable("id") id: Long): ResponseEntity<Empresa> {

        // TODO Elimina todos los eventos del Empresa
        //val listaEventos: List<Evento> = eventoService.getEventosByEmpresa(EmpresaService.get(id))
        //for (evento in listaEventos) {
          //  eventoService.delete(evento.getId())
        //}

        //Elmina el Empresa
        empresaService.delete(id)
        return ResponseEntity<Empresa>(HttpStatus.OK)
    }

    @GetMapping("/getAllExtraByEmpresaId/{id}")
    fun getListaExtra(@PathVariable("id") id: Long): ResponseEntity<MutableSet<Extra>>? {
        if (id != 0L) {
            return ResponseEntity<MutableSet<Extra>>(empresaService.get(id)?.listaExtra, HttpStatus.OK)
        }
        return ResponseEntity<MutableSet<Extra>>(HttpStatus.NO_CONTENT)
    }

    @GetMapping("/getAllTipoEventoByEmpresaId/{id}")
    fun getListaTipoEvento(@PathVariable("id") id: Long): ResponseEntity<MutableSet<TipoEvento>>? {
        if (id != 0L) {
            return ResponseEntity<MutableSet<TipoEvento>>(empresaService.get(id)?.listaTipoEvento, HttpStatus.OK)
        }
        return ResponseEntity<MutableSet<TipoEvento>>(HttpStatus.NO_CONTENT)
    }

    @GetMapping("/getAllServicioByEmpresaId/{id}")
    fun getAllServicioByEmpresaId(@PathVariable("id") id: Long): ResponseEntity<MutableSet<Servicio>>? {
        if (id != 0L) {
            return ResponseEntity<MutableSet<Servicio>>(empresaService.get(id)?.listaServicio, HttpStatus.OK)
        }
        return ResponseEntity<MutableSet<Servicio>>(HttpStatus.NO_CONTENT)
    }

}