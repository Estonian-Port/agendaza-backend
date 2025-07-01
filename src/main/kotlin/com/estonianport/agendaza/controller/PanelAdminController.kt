package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.CantidadesPanelAdminDTO
import com.estonianport.agendaza.service.EmpresaService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin("*")
class PanelAdminController {

    @Autowired
    lateinit var empresaService: EmpresaService

    @GetMapping("/getAllCantidadesForPanelAdminByEmpresaId/{id}")
    fun getAllCantidadesForPanelAdminByEmpresaId(@PathVariable("id") id: Long): CantidadesPanelAdminDTO {
        return empresaService.getAllCantidadesForPanelAdminByEmpresaId(id)
    }
}