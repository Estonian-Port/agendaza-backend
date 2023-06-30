package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.AgendaDto
import com.estonianport.agendaza.dto.EventoAgendaDto
import com.estonianport.agendaza.service.AgendaService
import com.estonianport.agendaza.service.EmpresaService
import com.estonianport.agendaza.service.EventoService
import com.estonianport.agendaza.service.UsuarioService
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
    lateinit var usuarioService : UsuarioService

    @Autowired
    lateinit var empresaService: EmpresaService

    @Autowired
    lateinit var eventoService: EventoService

    @GetMapping("/getListaAgendaByUsuarioId/{id}")
    fun getListaAgendaByUsuarioId(@PathVariable("id") id: Long): List<AgendaDto> {
        return agendaService.getListaAgendasByUsuario(usuarioService.findById(id)!!.listaCargo.toList())
    }

    @GetMapping("/getAllEventosForAgendaByEmpresaId/{id}")
    fun getAllEventosForAgendaByEmpresaId(@PathVariable("id") id: Long): List<EventoAgendaDto> {
        // TODO devuelve 34 querrys
        //return agendaService.getAllEventosForAgendaByEmpresaId(
        //  empresaService.findEmpresaById(id).listaEvento.toList()
        //)

        // TODO devuelve 18 querrys
        // TODO y usando @EntityGraph(attributePaths = ["capacidad", "encargado", "cliente", "tipoEvento.capacidad"])
        // TODO en el findAllByEmpresa devuelve 2 querrys
        return agendaService.getAllEventosForAgendaByEmpresaId(
            eventoService.findAllByEmpresa(
                empresaService.get(id)!!
        ).toList())
    }
}