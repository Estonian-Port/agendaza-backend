package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.AgendaDTO
import com.estonianport.agendaza.dto.EventoAgendaDTO
import com.estonianport.agendaza.dto.EventoDTO
import com.estonianport.agendaza.service.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

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
    fun getListaAgendaByUsuarioId(@PathVariable("id") usuarioId: Long): List<AgendaDTO> {
        return cargoService.getListaCargosByUsuarioId(usuarioId)
    }

    @GetMapping("/getAllEventosForAgendaByEmpresaId/{id}")
    fun getAllEventosForAgendaByEmpresaId(@PathVariable("id") id: Long): List<EventoAgendaDTO> {
        return eventoService.getAllEventosForAgendaByEmpresaId(id)
    }

    //TODO pasar a eventoController
    @GetMapping("/getAllEventosForAgendaByFecha")
    fun getAllEventosForAgendaByFecha(@RequestParam("fecha") fecha : String, @RequestParam("empresaId") empresaId : Long): List<EventoDTO> {
        return eventoService.getAllEventosForAgendaByFecha(fecha, empresaId)
    }

}