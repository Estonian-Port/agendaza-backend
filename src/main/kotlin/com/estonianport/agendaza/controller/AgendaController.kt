package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.AgendaDto
import com.estonianport.agendaza.dto.EventoAgendaDto
import com.estonianport.agendaza.service.*
import org.springframework.beans.factory.annotation.Autowired
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
    lateinit var eventoService: EventoService

    @Autowired
    lateinit var cargoService: CargoService

    @GetMapping("/getListaAgendaByUsuarioId/{id}")
    fun getListaAgendaByUsuarioId(@PathVariable("id") usuarioId: Long): List<AgendaDto> {
        return cargoService.getListaCargosByUsuarioId(usuarioId)
    }

    @GetMapping("/getAllEventosForAgendaByEmpresaId/{id}")
    fun getAllEventosForAgendaByEmpresaId(@PathVariable("id") id: Long): List<EventoAgendaDto> {
        return eventoService.getAllEventosForAgendaByEmpresaId(id)
    }
}