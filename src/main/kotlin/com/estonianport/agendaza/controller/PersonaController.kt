package com.estonianport.agendaza.controller

import com.estonianport.agendaza.model.Persona
import com.estonianport.agendaza.service.PersonaService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin("*")
class PersonaController {
    @Autowired
    lateinit var personaService: PersonaService

    @GetMapping("/getAllPersona")
    fun abm(): MutableList<Persona>? {
        return personaService.getAll()
    }

    @GetMapping("/getPersona/{id}")
    fun showSave(@PathVariable("id") id: Long): ResponseEntity<Persona>? {
        if (id != 0L) {
            ResponseEntity<Persona>(personaService.get(id), HttpStatus.OK)
        }
        return ResponseEntity<Persona>(HttpStatus.NO_CONTENT)
    }

    @PostMapping("/savePersona")
    fun save(Persona: Persona): ResponseEntity<Persona> {
        return ResponseEntity<Persona>(personaService.save(Persona), HttpStatus.OK)
    }

    @GetMapping("/deletePersona/{id}")
    fun delete(@PathVariable("id") id: Long): ResponseEntity<Persona> {
        personaService.delete(id)
        return ResponseEntity<Persona>(HttpStatus.OK)
    }
}