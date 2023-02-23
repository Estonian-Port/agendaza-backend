package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.EventoDto
import com.estonianport.agendaza.dto.PagoDto
import com.estonianport.agendaza.dto.UsuarioAbmDto
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.Pago
import com.estonianport.agendaza.model.Servicio
import com.estonianport.agendaza.model.TipoEvento
import com.estonianport.agendaza.model.TipoExtra
import com.estonianport.agendaza.service.EmpresaService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin("*")
class EmpresaController {

    @Autowired
    lateinit var empresaService: EmpresaService

    @GetMapping("/getAllEmpresa")
    fun getAll(): MutableList<Empresa>? {
        return empresaService.getAll()
    }

    @GetMapping("/getEmpresa/{id}")
    fun get(@PathVariable("id") id: Long): ResponseEntity<Empresa>? {
        if (id != 0L) {
            return ResponseEntity<Empresa>(empresaService.get(id), HttpStatus.OK)
        }
        return ResponseEntity<Empresa>(HttpStatus.NO_CONTENT)
    }

    @PostMapping("/saveEmpresa")
    fun save(@RequestBody empresa: Empresa): ResponseEntity<Empresa> {
        return ResponseEntity<Empresa>(empresaService.save(empresa), HttpStatus.OK)
    }

    @GetMapping("/getAllEventoByEmpresaId/{id}")
    fun getAllEventoByEmpresaId(@PathVariable("id") id: Long): ResponseEntity<MutableSet<EventoDto>>? {
        val empresa = empresaService.get(id)
        if(empresa != null){
            return ResponseEntity<MutableSet<EventoDto>>(empresaService.getAllEventoByEmpresaId(empresa), HttpStatus.OK)
        }
        return ResponseEntity<MutableSet<EventoDto>>(HttpStatus.NO_CONTENT)
    }

    @GetMapping("/getAllUsuarioByEmpresaId/{id}")
    fun getAllUsuariosByEmpresaId(@PathVariable("id") id: Long): ResponseEntity<MutableSet<UsuarioAbmDto>>? {
        val empresa = empresaService.get(id)
        if(empresa != null){
            return ResponseEntity<MutableSet<UsuarioAbmDto>>(empresaService.getAllUsuariosByEmpresaId(empresa), HttpStatus.OK)
        }
        return ResponseEntity<MutableSet<UsuarioAbmDto>>(HttpStatus.NO_CONTENT)
    }

    @GetMapping("/getAllExtraTipoEventoByEmpresaId/{id}")
    fun getAllExtraTipoEventoByEmpresaId(@PathVariable("id") id: Long): ResponseEntity<MutableSet<Extra>>? {
        if (id != 0L) {
            val listaExtra = empresaService.get(id)!!.listaExtra.filter{ it.tipoExtra == TipoExtra.EVENTO || it.tipoExtra == TipoExtra.VARIABLE_EVENTO }.toMutableSet()
            return ResponseEntity<MutableSet<Extra>>(listaExtra, HttpStatus.OK)
        }
        return ResponseEntity<MutableSet<Extra>>(HttpStatus.NO_CONTENT)
    }

    @GetMapping("/getAllExtraCateringByEmpresaId/{id}")
    fun getAllExtraCateringByEmpresaId(@PathVariable("id") id: Long): ResponseEntity<MutableSet<Extra>>? {
        if (id != 0L) {
            val listaExtra = empresaService.get(id)!!.listaExtra.filter{ it.tipoExtra == TipoExtra.TIPO_CATERING || it.tipoExtra == TipoExtra.VARIABLE_CATERING }.toMutableSet()
            return ResponseEntity<MutableSet<Extra>>(listaExtra, HttpStatus.OK)
        }
        return ResponseEntity<MutableSet<Extra>>(HttpStatus.NO_CONTENT)
    }

    @GetMapping("/getAllTipoEventoByEmpresaId/{id}")
    fun getListaTipoEvento(@PathVariable("id") id: Long): ResponseEntity<MutableSet<TipoEvento>>? {
        if (id != 0L) {
            return ResponseEntity<MutableSet<TipoEvento>>(empresaService.get(id)?.listaTipoEvento, HttpStatus.OK)
        }
        return ResponseEntity<MutableSet<TipoEvento>>(HttpStatus.NO_CONTENT)
    }

    @GetMapping("/getAllServicioByEmpresaId/{id}")
    fun getAllServicioByEmpresaId(@PathVariable("id") id: Long): ResponseEntity<MutableSet<Servicio>>? {
        if (id != 0L) {
            return ResponseEntity<MutableSet<Servicio>>(empresaService.get(id)?.listaServicio, HttpStatus.OK)
        }
        return ResponseEntity<MutableSet<Servicio>>(HttpStatus.NO_CONTENT)
    }

    @GetMapping("/getAllPagoByEmpresaId/{id}")
    fun getAllPagoByEmpresaId(@PathVariable("id") id: Long): ResponseEntity<MutableSet<PagoDto>>? {
        if (id != 0L) {
            val empresa = empresaService.get(id)
            if(empresa != null){
                return ResponseEntity<MutableSet<PagoDto>>(empresaService.getAllPagoByEmpresaId(empresa), HttpStatus.OK)
            }
        }
        return ResponseEntity<MutableSet<PagoDto>>(HttpStatus.NO_CONTENT)
    }

}