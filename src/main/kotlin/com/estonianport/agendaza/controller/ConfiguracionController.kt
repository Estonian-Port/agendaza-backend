package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.ConfiguracionDto
import com.estonianport.agendaza.dto.UsuarioAbmDto
import com.estonianport.agendaza.dto.UsuarioEmpresaDto
import com.estonianport.agendaza.model.Usuario
import com.estonianport.agendaza.service.AgendaService
import com.estonianport.agendaza.service.EmpresaService
import com.estonianport.agendaza.service.UsuarioService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin("*")
class ConfiguracionController {

    @Autowired
    lateinit var empresaService: EmpresaService

    @Autowired
    lateinit var agendaService: AgendaService

    @Autowired
    lateinit var usuarioService: UsuarioService

    @PutMapping("/getAllCantidadesConfiguracionByUsuarioIdAndEmpresaId")
    fun getAllCantidadesConfiguracionByUsuarioIdAndEmpresaId(@RequestBody usuarioEmpresaDto: UsuarioEmpresaDto): ResponseEntity<ConfiguracionDto>? {
        val usuario = usuarioService.get(usuarioEmpresaDto.usuarioId)
        val empresa = empresaService.get(usuarioEmpresaDto.empresaId)

        if(usuario != null && empresa != null){
            return ResponseEntity<ConfiguracionDto>(agendaService.getAllCantidadesConfiguracionByUsuarioAndEmpresa(usuario, empresa), HttpStatus.OK)
        }
        return ResponseEntity<ConfiguracionDto>(HttpStatus.NO_CONTENT)
    }
}