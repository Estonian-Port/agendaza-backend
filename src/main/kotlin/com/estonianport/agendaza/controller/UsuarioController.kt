package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.GenericItemDto
import com.estonianport.agendaza.dto.UsuarioDto
import com.estonianport.agendaza.model.Cargo
import com.estonianport.agendaza.model.Sexo
import com.estonianport.agendaza.model.TipoCargoNombre
import com.estonianport.agendaza.model.Usuario
import com.estonianport.agendaza.service.CargoService
import com.estonianport.agendaza.service.EmpresaService
import com.estonianport.agendaza.service.UsuarioService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin("*")
class UsuarioController {

    @Autowired
    lateinit var usuarioService: UsuarioService

    @Autowired
    lateinit var empresaService: EmpresaService

    @Autowired
    lateinit var cargoService: CargoService


    @GetMapping("/getAllUsuario")
    fun abm(): MutableList<Usuario>? {
        return usuarioService.getAll()
    }

    @GetMapping("/getUsuario/{id}")
    fun showSave(@PathVariable("id") id: Long): ResponseEntity<Usuario>? {
        if (id != 0L) {
            ResponseEntity<Usuario>(usuarioService.get(id), HttpStatus.OK)
        }
        return ResponseEntity<Usuario>(HttpStatus.NO_CONTENT)
    }

    @PostMapping("/saveUsuario")
    fun save(@RequestBody usuarioDto: UsuarioDto): ResponseEntity<Usuario> {
        usuarioDto.usuario.password = BCryptPasswordEncoder().encode(usuarioDto.usuario.password)

        val usuario = usuarioService.save(usuarioDto.usuario)
        val empresa = empresaService.get(usuarioDto.empresaId)
        if(empresa != null){
            cargoService.save(Cargo(0,usuario,empresa, usuarioDto.rol))
        }

        return ResponseEntity<Usuario>(usuario, HttpStatus.OK)
    }

    @PutMapping("/disableUsuario/{id}")
    fun disable(@PathVariable("id") id: Long): ResponseEntity<Usuario> {
        usuarioService.delete(id)
        return ResponseEntity<Usuario>(HttpStatus.OK)
    }

    @PutMapping("/getUsuarioIdByUsername")
    fun getUsuarioIdByUsername(@RequestBody username: String): Long {
        return usuarioService.getUsuarioIdByUsername(username)
    }

    @GetMapping("/getAllEmpresaByUsuarioId/{id}")
    fun getAllEmpresaByUsuarioId(@PathVariable("id") id: Long): ResponseEntity<MutableSet<GenericItemDto>>? {
        val usuario = usuarioService.get(id)

        if(usuario != null){
            return ResponseEntity<MutableSet<GenericItemDto>>(usuarioService.getAllEmpresaByUsuario(usuario), HttpStatus.OK)
        }

        return ResponseEntity<MutableSet<GenericItemDto>>(HttpStatus.NO_CONTENT)
    }

    @GetMapping("/getAllRol")
    fun getAllRoles(): ResponseEntity<MutableSet<TipoCargoNombre>>? {
        return ResponseEntity<MutableSet<TipoCargoNombre>>(TipoCargoNombre.values().toMutableSet(), HttpStatus.OK)
    }

    @GetMapping("/getAllSexo")
    fun getAllSexo(): ResponseEntity<MutableSet<Sexo>>? {
        return ResponseEntity<MutableSet<Sexo>>(Sexo.values().toMutableSet(), HttpStatus.OK)
    }

}