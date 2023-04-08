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
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.LocalDateTime

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
    fun get(@PathVariable("id") id: Long): Empresa? {
        return empresaService.get(id)
    }

    @PostMapping("/saveEmpresa")
    fun save(@RequestBody empresa: Empresa): Empresa {
        return empresaService.save(empresa)
    }

    @GetMapping("/getAllEventoByEmpresaId/{id}")
    fun getAllEventoByEmpresaId(@PathVariable("id") id: Long): MutableSet<EventoDto> {
        val empresa = empresaService.get(id)!!
        return empresaService.getAllEventoByEmpresaId(empresa)
    }

    @PutMapping("/getAllEventoByEmpresaIdAndFechaFiltro/{id}")
    fun getAllEventoByEmpresaIdAndFechaFiltro(@PathVariable("id") id: Long, @RequestBody fechaFiltro : LocalDate): MutableSet<EventoDto> {
        val empresa = empresaService.get(id)!!
        val listaEventos = empresaService.getAllEventoByEmpresaId(empresa)
        return listaEventos.filter { it.inicio.toLocalDate() == fechaFiltro }.toMutableSet()
    }

    @GetMapping("/getAllUsuarioByEmpresaId/{id}")
    fun getAllUsuariosByEmpresaId(@PathVariable("id") id: Long): MutableSet<UsuarioAbmDto> {
        val empresa = empresaService.get(id)!!
        return empresaService.getAllUsuariosByEmpresaId(empresa)
    }

    @GetMapping("/getAllExtraTipoEventoByEmpresaId/{id}")
    fun getAllExtraTipoEventoByEmpresaId(@PathVariable("id") id: Long): MutableSet<Extra> {
        return empresaService.get(id)!!.
            listaExtra.filter{ it.tipoExtra == TipoExtra.EVENTO || it.tipoExtra == TipoExtra.VARIABLE_EVENTO }.toMutableSet()
    }

    @GetMapping("/getAllExtraCateringByEmpresaId/{id}")
    fun getAllExtraCateringByEmpresaId(@PathVariable("id") id: Long): MutableSet<Extra> {
        return empresaService.get(id)!!.
        listaExtra.filter{ it.tipoExtra == TipoExtra.TIPO_CATERING || it.tipoExtra == TipoExtra.VARIABLE_CATERING }.toMutableSet()
    }

    @GetMapping("/getAllTipoEventoByEmpresaId/{id}")
    fun getAllTipoEventoByEmpresaId(@PathVariable("id") id: Long): MutableSet<TipoEvento> {
        return empresaService.get(id)!!.listaTipoEvento
    }

    @GetMapping("/getAllServicioByEmpresaId/{id}")
    fun getAllServicioByEmpresaId(@PathVariable("id") id: Long): MutableSet<Servicio> {
        return empresaService.get(id)!!.listaServicio
    }

    @GetMapping("/getAllPagoByEmpresaId/{id}")
    fun getAllPagoByEmpresaId(@PathVariable("id") id: Long): MutableSet<PagoDto> {
        val empresa = empresaService.get(id)!!
        return empresaService.getAllPagoByEmpresaId(empresa)
    }

}