package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.AgendaDto
import com.estonianport.agendaza.dto.AgendaEventoDto
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
class AgendaController {

    @Autowired
    lateinit var agendaService : AgendaService

    @Autowired
    lateinit var usuarioService : UsuarioService

    @Autowired
    lateinit var empresaService: EmpresaService

    @GetMapping("/getListaAgendaByUsuarioId/{id}")
    fun getListaAgendaByUsuarioId(@PathVariable("id") id: Long): ResponseEntity<MutableSet<AgendaDto>>? {
        if (id != 0L) {
            val usuario = usuarioService.get(id)
            if(usuario != null){
                return ResponseEntity<MutableSet<AgendaDto>>(agendaService.getListaAgendasByUsuario(usuario), HttpStatus.OK)
            }
        }
        return ResponseEntity<MutableSet<AgendaDto>>(HttpStatus.NO_CONTENT)
    }

    @GetMapping("/getAllEventosForAgendaByEmpresaId/{id}")
    fun getAllEventosForAgendaByEmpresaId(@PathVariable("id") id: Long):  ResponseEntity<MutableSet<AgendaEventoDto>>? {
        val empresa = empresaService.get(id)
        if(empresa != null){
            return ResponseEntity<MutableSet<AgendaEventoDto>>(agendaService.getAllEventosForAgendaByEmpresaId(empresa), HttpStatus.OK)
        }
        return ResponseEntity<MutableSet<AgendaEventoDto>>(HttpStatus.NO_CONTENT)
    }
}