package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.EventoDto
import com.estonianport.agendaza.dto.UsuarioAbmDto
import com.estonianport.agendaza.model.Evento
import com.estonianport.agendaza.service.EmpresaService
import com.estonianport.agendaza.service.EventoService
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
class EventoController {

    @Autowired
    lateinit var eventoService: EventoService

    @Autowired
    lateinit var empresaService : EmpresaService

    @GetMapping("/getAllEvento")
    fun getAll(): MutableList<Evento>? {
        return eventoService.getAll()
    }

    @GetMapping("/getEvento/{id}")
    fun get(@PathVariable("id") id: Long): ResponseEntity<Evento>? {
        if (id != 0L) {
            return ResponseEntity<Evento>(eventoService.get(id), HttpStatus.OK)
        }
        return ResponseEntity<Evento>(HttpStatus.NO_CONTENT)
    }

    @PostMapping("/saveEvento")
    fun save(@RequestBody evento: Evento): ResponseEntity<Evento> {
        return ResponseEntity<Evento>(eventoService.save(evento), HttpStatus.OK)
    }

    @DeleteMapping("/deleteEvento/{id}")
    fun delete(@PathVariable("id") id: Long): ResponseEntity<Evento> {
        eventoService.delete(id)
        return ResponseEntity<Evento>(HttpStatus.OK)
    }

    @GetMapping("/getAllEventoByEmpresaId/{id}")
    fun getAllEventoByEmpresaId(@PathVariable("id") id: Long): ResponseEntity<MutableSet<EventoDto>>? {
        val empresa = empresaService.get(id)
        if(empresa != null){
            return ResponseEntity<MutableSet<EventoDto>>(empresaService.getAllEventoByEmpresaId(empresa), HttpStatus.OK)
        }
        return ResponseEntity<MutableSet<EventoDto>>(HttpStatus.NO_CONTENT)
    }
}