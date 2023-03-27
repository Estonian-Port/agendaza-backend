package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.EventoDto
import com.estonianport.agendaza.dto.GenericItemDto
import com.estonianport.agendaza.model.Servicio
import com.estonianport.agendaza.model.TipoEvento
import com.estonianport.agendaza.service.EmpresaService
import com.estonianport.agendaza.service.ServicioService
import com.estonianport.agendaza.service.TipoEventoService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin("*")
class ServicioController {

    @Autowired
    lateinit var servicioService: ServicioService

    @Autowired
    lateinit var tipoEventoService : TipoEventoService

    @Autowired
    lateinit var empresaService: EmpresaService

    @GetMapping("/getAllServicio")
    fun getAll(): MutableList<Servicio>? {
        return servicioService.getAll()
    }

    @GetMapping("/getServicio/{id}")
    fun get(@PathVariable("id") id: Long): GenericItemDto {
        val servicio = servicioService.get(id)!!

        val genericItemDto = GenericItemDto(servicio.id, servicio.nombre)
        genericItemDto.empresaId = servicio.empresa.id
        genericItemDto.listaTipoEventoId = servicio.listaTipoEvento.map { it.id }.toMutableSet()

        return genericItemDto
    }

    @GetMapping("/getServicioListaTipoEvento/{id}")
    fun getListaTipoEvento(@PathVariable("id") id: Long): MutableSet<TipoEvento> {
        return servicioService.get(id)!!.listaTipoEvento
    }

    @PostMapping("/saveServicio")
    fun save(@RequestBody genericItemDto: GenericItemDto): Servicio {
        val empresa = empresaService.get(genericItemDto.empresaId)!!

        val listaTipoEvento : MutableSet<TipoEvento> = mutableSetOf()

        genericItemDto.listaTipoEventoId.forEach {
            tipoEventoService.get(it)?.let { it1 -> listaTipoEvento.add(it1) }
        }

        val servicio = Servicio(genericItemDto.id, genericItemDto.nombre, empresa)
        servicio.listaTipoEvento = listaTipoEvento

        return servicioService.save(servicio)

    }

    @DeleteMapping("/deleteServicio/{id}")
    fun delete(@PathVariable("id") id : Long): ResponseEntity<Servicio> {
        servicioService.delete(id)
        return ResponseEntity<Servicio>(HttpStatus.OK)
    }
}