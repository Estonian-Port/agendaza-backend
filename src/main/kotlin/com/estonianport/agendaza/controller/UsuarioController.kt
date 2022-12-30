package com.estonianport.agendaza.controller

import com.estonianport.agendaza.model.Usuario
import com.estonianport.agendaza.service.UsuarioService
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
class UsuarioController {
    @Autowired
    lateinit var UsuarioService: UsuarioService

    @GetMapping("/getAllUsuario")
    fun abm(): MutableList<Usuario>? {
        return UsuarioService.getAll()
    }

    @GetMapping("/getUsuario/{id}")
    fun showSave(@PathVariable("id") id: Long): ResponseEntity<Usuario>? {
        if (id != 0L) {
            ResponseEntity<Usuario>(UsuarioService.get(id), HttpStatus.OK)
        }
        return ResponseEntity<Usuario>(HttpStatus.NO_CONTENT)
    }

    @PostMapping("/saveUsuario")
    fun save(@RequestBody Usuario: Usuario): ResponseEntity<Usuario> {
        return ResponseEntity<Usuario>(UsuarioService.save(Usuario), HttpStatus.OK)
    }

    @GetMapping("/deleteUsuario/{id}")
    fun delete(@PathVariable("id") id: Long): ResponseEntity<Usuario> {
        UsuarioService.delete(id)
        return ResponseEntity<Usuario>(HttpStatus.OK)
    }
}