package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.AgendaDto
import com.estonianport.agendaza.dto.EventoAgendaDto
import com.estonianport.agendaza.dto.EventoDto
import com.estonianport.agendaza.service.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

    //TODO pasar a eventoController
    @GetMapping("/getAllEventosForAgendaByFecha")
    fun getAllEventosForAgendaByFecha(@RequestParam("fecha") fecha : String, @RequestParam("empresaId") empresaId : Long): List<EventoDto> {
        return eventoService.getAllEventosForAgendaByFecha(fecha, empresaId)
    }

}