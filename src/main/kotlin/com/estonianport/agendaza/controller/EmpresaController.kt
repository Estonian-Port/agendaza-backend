package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.EventoDto
import com.estonianport.agendaza.dto.PagoDto
import com.estonianport.agendaza.dto.TipoEventoDTO
import com.estonianport.agendaza.dto.UsuarioAbmDto
import com.estonianport.agendaza.model.Duracion
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.Servicio
import com.estonianport.agendaza.model.TipoExtra
import com.estonianport.agendaza.service.EmpresaService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

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
    fun getAllEventoByEmpresaId(@PathVariable("id") id: Long): List<EventoDto> {
        return empresaService.getAllEventoByEmpresaId(empresaService.findEmpresaById(id))
    }

    @PutMapping("/getAllEventoByEmpresaIdAndFechaFiltro/{id}")
    fun getAllEventoByEmpresaIdAndFechaFiltro(@PathVariable("id") id: Long, @RequestBody fechaFiltro : LocalDate): MutableSet<EventoDto> {
        val listaEventos = empresaService.getAllEventoByEmpresaId(empresaService.findEmpresaById(id))
        return listaEventos.filter { it.inicio.toLocalDate() == fechaFiltro }.toMutableSet()
    }

    @GetMapping("/getAllUsuarioByEmpresaId/{id}")
    fun getAllUsuariosByEmpresaId(@PathVariable("id") id: Long): List<UsuarioAbmDto> {
        return empresaService.getAllUsuariosByEmpresaId(empresaService.get(id)!!)
    }

    //TODO refactor con service getAllExtraTipoEvento
    @GetMapping("/getAllExtraTipoEventoByEmpresaId/{id}")
    fun getAllExtraTipoEventoByEmpresaId(@PathVariable("id") id: Long): MutableSet<Extra> {
        return empresaService.get(id)!!.
            listaExtra.filter{ (it.tipoExtra == TipoExtra.EVENTO || it.tipoExtra == TipoExtra.VARIABLE_EVENTO) && it.fechaBaja == null }.toMutableSet()
    }

    //TODO refactor con service getAllExtraCatering
    @GetMapping("/getAllExtraCateringByEmpresaId/{id}")
    fun getAllExtraCateringByEmpresaId(@PathVariable("id") id: Long): MutableSet<Extra> {
        return empresaService.get(id)!!.
        listaExtra.filter{ (it.tipoExtra == TipoExtra.TIPO_CATERING || it.tipoExtra == TipoExtra.VARIABLE_CATERING)  && it.fechaBaja == null }.toMutableSet()
    }

    @GetMapping("/getAllTipoEventoByEmpresaId/{id}")
    fun getAllTipoEventoByEmpresaId(@PathVariable("id") id: Long): List<TipoEventoDTO> {
        return empresaService.get(id)!!.listaTipoEvento.filter {
            it.fechaBaja == null }.map { it.toDTO() }
    }

    @PutMapping("/getAllTipoEventoByEmpresaIdAndDuracion/{id}")
    fun getAllTipoEventoByEmpresaIdAndDuracion(@PathVariable("id") id : Long, @RequestBody duracion : String): List<TipoEventoDTO> {
        return empresaService.get(id)!!.listaTipoEvento.filter {
            it.fechaBaja == null && it.duracion.name == duracion }.map { it.toDTO() }
    }

    @GetMapping("/getAllServicioByEmpresaId/{id}")
    fun getAllServicioByEmpresaId(@PathVariable("id") id: Long): List<Servicio> {
        return empresaService.get(id)!!.listaServicio.filter { it.fechaBaja == null }
    }

    @GetMapping("/getAllPagoByEmpresaId/{id}")
    fun getAllPagoByEmpresaId(@PathVariable("id") id: Long): List<PagoDto> {
        return empresaService.getAllPagoByEmpresaId(empresaService.getEmpresaListaPagoById(id))
    }

}