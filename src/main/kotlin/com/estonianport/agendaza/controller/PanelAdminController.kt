package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.ConfiguracionDto
import com.estonianport.agendaza.dto.UsuarioEmpresaDto
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.service.AgendaService
import com.estonianport.agendaza.service.EmpresaService
import com.estonianport.agendaza.service.UsuarioService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin("*")
class PanelAdminController {

    @Autowired
    lateinit var empresaService: EmpresaService

    @Autowired
    lateinit var agendaService: AgendaService

    @Autowired
    lateinit var usuarioService: UsuarioService

    @PutMapping("/getAllCantidadesForPanelAdminByUsuarioIdAndEmpresaId")
    fun getAllCantidadesConfiguracionByUsuarioIdAndEmpresaId(@RequestBody usuarioEmpresaDto: UsuarioEmpresaDto): ConfiguracionDto {
        val usuario = usuarioService.findById(usuarioEmpresaDto.usuarioId)!!
        val empresa = empresaService.findById(usuarioEmpresaDto.empresaId)

        return agendaService.getAllCantidadesConfiguracionByUsuarioAndEmpresa(usuario, empresa)
    }
}