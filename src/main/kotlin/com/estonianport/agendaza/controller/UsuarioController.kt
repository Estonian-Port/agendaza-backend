package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.*
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

    @GetMapping("/getUsuarioPerfil/{usuarioId}")
    fun getUsuario(@PathVariable("usuarioId") usuarioId: Long): UsuarioPerfilDTO {
        return usuarioService.getUsuarioPerfil(usuarioId)
    }

    @GetMapping("/getUsuarioOfEmpresa/{usuarioId}/{empresaId}")
    fun getUsuario(@PathVariable("usuarioId") usuarioId: Long, @PathVariable("empresaId") empresaId: Long): UsuarioEditCargoDTO {
        return usuarioService.getUsuarioOfEmpresa(usuarioId, empresaId)
    }

    //TODO Borrar, reemplazado por cargoController.getCargoByEmpresaAndUsuario()
    @PutMapping("/getRolByUsuarioIdAndEmpresaId")
    fun getRolByUsuarioIdAndEmpresaId(@RequestBody usuarioEmpresaDto: UsuarioEmpresaDTO): TipoCargo? {
        val usuario = usuarioService.get(usuarioEmpresaDto.usuarioId)!!
        return usuario.listaCargo.find{ it.empresa.id == usuarioEmpresaDto.empresaId}!!.tipoCargo
    }

    @PostMapping("/saveUsuario")
    fun save(@RequestBody usuarioDto: UsuarioDTO): Usuario {
        // Si llega por primera vez se encripta la contraseña sino se deja igual
        // para cambiar contraseña se debe usar editPassword
        if (usuarioDto.usuario.id == 0L) {
            usuarioDto.usuario.password = BCryptPasswordEncoder().encode(usuarioDto.usuario.password)
        }else{
            usuarioDto.usuario.password = usuarioService.findById(usuarioDto.usuario.id)!!.password
        }

        val usuario = usuarioService.save(usuarioDto.usuario)

        if(usuarioDto.empresaId != 0L && usuarioDto.cargo != null) {
            val empresa = empresaService.get(usuarioDto.empresaId)
            if (empresa != null) {

                val cargoOld = empresa.listaEmpleados.find { it.usuario.id == usuario.id }
                // Si tenia un cargo y se cambio, se le modifica
                if (cargoOld != null) {
                    cargoOld.tipoCargo = usuarioDto.cargo!!
                    cargoService.save(cargoOld)
                } else {
                    cargoService.save(Cargo(0, usuario, empresa, usuarioDto.cargo!!))
                }
            }
        }
        return usuario
    }

    @PostMapping("/editPassword")
    fun editPassword(@RequestBody usuarioEditPasswordDto: UsuarioEditPasswordDTO): Usuario? {
        val usuario = usuarioService.get(usuarioEditPasswordDto.id)!!
        usuario.password = BCryptPasswordEncoder().encode(usuarioEditPasswordDto.password)
        return usuarioService.save(usuario)
    }

    @PutMapping("/getUsuarioIdByUsername")
    fun getUsuarioIdByUsername(@RequestBody username: String): Long {
        return usuarioService.getUsuarioIdByUsername(username)
    }

    @GetMapping("/getAllEmpresaByUsuarioId/{id}")
    fun getAllEmpresaByUsuarioId(@PathVariable("id") id: Long): List<GenericItemDTO> {
        return usuarioService.getAllEmpresaByUsuario(usuarioService.get(id)!!)
    }

    @PostMapping("/saveUsuarioCargoOfEmpresa")
    fun saveUsuarioCargoOfEmpresa(@RequestBody usuarioEditCargoDTO: UsuarioEditCargoDTO): Long {
        val cargo : Cargo = cargoService.getCargoByEmpresaIdAndUsuarioId(usuarioEditCargoDTO.empresaId, usuarioEditCargoDTO.id)
        cargo.tipoCargo = usuarioEditCargoDTO.cargo
        return cargoService.save(cargo).id
    }

    @GetMapping("/getAllUsuariosByEmpresaId/{empresaId}/{pageNumber}")
    fun getAllUsuarios(@PathVariable("empresaId") empresaId: Long, @PathVariable("pageNumber") pageNumber: Int): List<UsuarioAbmDTO> {
        return usuarioService.getAllUsuariosByEmpresaId(empresaId, pageNumber)
    }

    @GetMapping("/getAllUsersByFilterName/{empresaId}/{pageNumber}/{buscar}")
    fun getAllUsuarioByFilterName(@PathVariable("empresaId") empresaId: Long, @PathVariable("pageNumber") pageNumber: Int, @PathVariable("buscar") buscar: String): List<UsuarioAbmDTO> {
        return usuarioService.getAllUsuarioByFilterName(empresaId, pageNumber, buscar)
    }

    @GetMapping("/cantUsuarios/{empresaId}")
    fun cantidadUsuario(@PathVariable("empresaId") empresaId: Long) =  usuarioService.cantidadUsuario(empresaId)

    @GetMapping("/cantUsuariosFiltrados/{empresaId}/{buscar}")
    fun cantidadUsuarioFiltrados(@PathVariable("empresaId") empresaId: Long, @PathVariable("buscar") buscar : String) =  usuarioService.cantidadUsuarioFiltrados(empresaId,buscar)


}