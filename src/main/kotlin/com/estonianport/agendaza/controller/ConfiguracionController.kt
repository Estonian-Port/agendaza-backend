package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.AgendaDto
import com.estonianport.agendaza.dto.AgendaEventoDto
import com.estonianport.agendaza.dto.ConfiguracionDto
import com.estonianport.agendaza.service.AgendaService
import com.estonianport.agendaza.service.EmpresaService
import com.estonianport.agendaza.service.UsuarioService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin("*")
class ConfiguracionController {

    @Autowired
    lateinit var empresaService: EmpresaService

    @Autowired
    lateinit var agendaService: AgendaService

    @GetMapping("/getAllCantidadesConfiguracionByEmpresa/{id}")
    fun getAllEventosByEmpresaId(@PathVariable("id") id: Long): ResponseEntity<ConfiguracionDto>? {
        val empresa = empresaService.get(id)
        if(empresa != null){
            return ResponseEntity<ConfiguracionDto>(agendaService.getAllCantidadesConfiguracionByEmpresa(empresa), HttpStatus.OK)
        }
        return ResponseEntity<ConfiguracionDto>(HttpStatus.NO_CONTENT)
    }
}