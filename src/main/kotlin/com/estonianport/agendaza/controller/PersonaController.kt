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
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin("*")
class PersonaController {
    @Autowired
    lateinit var PersonaService: PersonaService

    @GetMapping("/getAllPersona")
    fun abm(): MutableList<Persona>? {
        return PersonaService.getAll()
    }

    @GetMapping("/getPersona/{id}")
    fun showSave(@PathVariable("id") id: Long): ResponseEntity<Persona>? {
        if (id != 0L) {
            ResponseEntity<Persona>(PersonaService.get(id), HttpStatus.OK)
        }
        return ResponseEntity<Persona>(HttpStatus.NO_CONTENT)
    }

    @PostMapping("/savePersona")
    fun save(@RequestBody persona: Persona): ResponseEntity<Persona> {
        return ResponseEntity<Persona>(PersonaService.save(persona), HttpStatus.OK)
    }

    @GetMapping("/deletePersona/{id}")
    fun delete(@PathVariable("id") id: Long): ResponseEntity<Persona> {
        PersonaService.delete(id)
        return ResponseEntity<Persona>(HttpStatus.OK)
    }
}