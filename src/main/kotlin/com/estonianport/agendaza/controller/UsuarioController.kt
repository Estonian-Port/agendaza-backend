package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.GenericItemDto
import com.estonianport.agendaza.dto.UsuarioDto
import com.estonianport.agendaza.dto.UsuarioEditPasswordDto
import com.estonianport.agendaza.dto.UsuarioEmpresaDto
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.Cargo
import com.estonianport.agendaza.model.Sexo
import com.estonianport.agendaza.model.TipoCargoNombre
import com.estonianport.agendaza.model.Usuario
import com.estonianport.agendaza.service.CargoService
import com.estonianport.agendaza.service.EmpresaService
import com.estonianport.agendaza.service.UsuarioService
import jakarta.persistence.NonUniqueResultException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.userdetails.UsernameNotFoundException
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

    @PutMapping("/getUsuarioByDni")
    fun getUsuarioByDni(@RequestBody dni: Long): ResponseEntity<Usuario>? {
        try {
            val usuario = usuarioService.getUsuarioByDni(dni)?:
                throw NotFoundException("No se encontró el Cliente")

            return ResponseEntity<Usuario>(usuario, HttpStatus.OK)
        }catch (e : Exception){
            throw NotFoundException("No se encontró el Cliente")
        }
    }

    @PutMapping("/getUsuarioByEmail")
    fun getUsuarioByEmail(@RequestBody email : String): ResponseEntity<Usuario>? {
        try {
            val usuario = usuarioService.getUsuarioByEmail(email)?:
                throw NotFoundException("No se encontró el Cliente")

            return ResponseEntity<Usuario>(usuario, HttpStatus.OK)
        }catch (e : Exception){
            throw NotFoundException("No se encontró el Cliente")
        }
    }

    @PutMapping("/getUsuarioByCelular")
    fun getUsuarioByCelular(@RequestBody celular : Long): ResponseEntity<Usuario>? {
        try {
            val usuario = usuarioService.getUsuarioByCelular(celular)?:
                throw NotFoundException("No se encontró el Cliente")

            return ResponseEntity<Usuario>(usuario, HttpStatus.OK)
        }catch (e : Exception){
            throw NotFoundException("No se encontró el Cliente")
        }
    }

    @GetMapping("/getUsuario/{id}")
    fun getUsuario(@PathVariable("id") id: Long): ResponseEntity<Usuario>? {
        if (id != 0L) {
            return ResponseEntity<Usuario>(usuarioService.get(id), HttpStatus.OK)
        }
        return ResponseEntity<Usuario>(HttpStatus.NO_CONTENT)
    }

    @PutMapping("/getRolByUsuarioIdAndEmpresaId")
    fun getRolByUsuarioIdAndEmpresaId(@RequestBody usuarioEmpresaDto: UsuarioEmpresaDto): ResponseEntity<TipoCargoNombre>? {
        val usuario = usuarioService.get(usuarioEmpresaDto.usuarioId)
        if(usuario != null){
            val cargo = usuario.listaCargo.find{ it.empresa.id == usuarioEmpresaDto.empresaId}
            if(cargo != null){
                return ResponseEntity<TipoCargoNombre>(cargo.tipoCargo ,HttpStatus.OK)
            }
        }
        return ResponseEntity<TipoCargoNombre>(HttpStatus.NO_CONTENT)
    }

    @PostMapping("/saveUsuario")
    fun save(@RequestBody usuarioDto: UsuarioDto): ResponseEntity<Usuario> {
        // Si llega por primera vez se encripta la contraseña sino se deja igual
        // para cambiar contraseña se debe usar editPassword
        if (usuarioDto.usuario.id == 0L) {
            usuarioDto.usuario.password = BCryptPasswordEncoder().encode(usuarioDto.usuario.password)
        }

        val usuario = usuarioService.save(usuarioDto.usuario)

        val empresa = empresaService.get(usuarioDto.empresaId)

        if(empresa != null) {

            val cargoOld = empresa.listaEmpleados.find { it.usuario.id == usuario.id }

            if (cargoOld != null){
                cargoOld.tipoCargo = usuarioDto.rol
                cargoService.save(cargoOld)
            }else{
                cargoService.save(Cargo(0, usuario, empresa, usuarioDto.rol))
            }
        }
        return ResponseEntity<Usuario>(usuario, HttpStatus.OK)
    }

    @PostMapping("/editPassword")
    fun editPassword(@RequestBody usuarioEditPasswordDto: UsuarioEditPasswordDto): ResponseEntity<Usuario>? {
        val usuario = usuarioService.get(usuarioEditPasswordDto.id)
        if(usuario != null) {
            usuario.password = BCryptPasswordEncoder().encode(usuarioEditPasswordDto.password)
            return ResponseEntity<Usuario>(usuarioService.save(usuario), HttpStatus.OK)
        }
        return ResponseEntity<Usuario>(HttpStatus.NO_CONTENT)
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