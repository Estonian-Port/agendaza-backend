package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.GenericItemDto
import com.estonianport.agendaza.dto.UsuarioDto
import com.estonianport.agendaza.dto.UsuarioEditPasswordDto
import com.estonianport.agendaza.dto.UsuarioEmpresaDto
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.Cargo
import com.estonianport.agendaza.model.TipoCargo
import com.estonianport.agendaza.model.Usuario
import com.estonianport.agendaza.service.CargoService
import com.estonianport.agendaza.service.EmpresaService
import com.estonianport.agendaza.service.UsuarioService
import org.springframework.beans.factory.annotation.Autowired
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
    @GetMapping("/cantUsuarios/{id}")
    fun cantUsuarios(@PathVariable("id") id: Long) =  usuarioService.contadorDeUsuarios(id)
    @GetMapping("/cantUsuariosFiltrados/{id}/{buscar}")
    fun cantUsuariosFiltrados(@PathVariable("id") id: Long, @PathVariable("buscar") buscar : String) =  usuarioService.contadorDeUsuariosFiltrados(id,buscar)
    @PutMapping("/getUsuarioByEmail")
    fun getUsuarioByEmail(@RequestBody email : String): Usuario? {
        try {
            return usuarioService.getUsuarioByEmail(email)?:
                throw NotFoundException("No se encontró el Cliente")
        }catch (e : Exception){
            throw NotFoundException("No se encontró el Cliente")
        }
    }

    @PutMapping("/getUsuarioByCelular")
    fun getUsuarioByCelular(@RequestBody celular : Long): Usuario? {
        try {
            return usuarioService.getUsuarioByCelular(celular)?:
                throw NotFoundException("No se encontró el Cliente")
        }catch (e : Exception){
            throw NotFoundException("No se encontró el Cliente")
        }
    }

    @GetMapping("/getUsuario/{id}")
    fun getUsuario(@PathVariable("id") id: Long): Usuario? {
        return usuarioService.get(id)
    }

    @PutMapping("/getRolByUsuarioIdAndEmpresaId")
    fun getRolByUsuarioIdAndEmpresaId(@RequestBody usuarioEmpresaDto: UsuarioEmpresaDto): TipoCargo? {
        val usuario = usuarioService.get(usuarioEmpresaDto.usuarioId)!!
        return usuario.listaCargo.find{ it.empresa.id == usuarioEmpresaDto.empresaId}!!.tipoCargo
    }

    @PostMapping("/saveUsuario")
    fun save(@RequestBody usuarioDto: UsuarioDto): Usuario {
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
        return usuario
    }

    @PostMapping("/editPassword")
    fun editPassword(@RequestBody usuarioEditPasswordDto: UsuarioEditPasswordDto): Usuario? {
        val usuario = usuarioService.get(usuarioEditPasswordDto.id)!!
        usuario.password = BCryptPasswordEncoder().encode(usuarioEditPasswordDto.password)
        return usuarioService.save(usuario)
    }

    @PutMapping("/getUsuarioIdByUsername")
    fun getUsuarioIdByUsername(@RequestBody username: String): Long {
        return usuarioService.getUsuarioIdByUsername(username)
    }

    @GetMapping("/getAllEmpresaByUsuarioId/{id}")
    fun getAllEmpresaByUsuarioId(@PathVariable("id") id: Long): List<GenericItemDto> {
        return usuarioService.getAllEmpresaByUsuario(usuarioService.get(id)!!)
    }

    @GetMapping("/getAllRol")
    fun getAllRoles(): MutableSet<TipoCargo> {
        return TipoCargo.values().toMutableSet()
    }

}