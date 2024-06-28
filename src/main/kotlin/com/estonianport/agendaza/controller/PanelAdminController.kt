package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.CantidadesPanelAdmin
import com.estonianport.agendaza.service.AgendaService
import com.estonianport.agendaza.service.EmpresaService
import com.estonianport.agendaza.service.UsuarioService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin("*")
class PanelAdminController {

    @Autowired
    lateinit var empresaService: EmpresaService

    @GetMapping("/getAllCantidadesForPanelAdminByEmpresaId/{id}")
    fun getAllCantidadesForPanelAdminByEmpresaId(@PathVariable("id") id: Long): CantidadesPanelAdmin {
        return empresaService.getAllCantidadesForPanelAdminByEmpresaId(id)
    }
}