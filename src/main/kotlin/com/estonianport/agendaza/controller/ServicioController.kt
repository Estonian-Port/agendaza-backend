package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.GenericItemDto
import com.estonianport.agendaza.dto.ServicioDTO
import com.estonianport.agendaza.model.Servicio
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
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

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
    fun get(@PathVariable("id") id: Long): ServicioDTO {
        val servicio = servicioService.get(id)!!

        val servicioDTO = servicio.toDTO()
        servicioDTO.listaTipoEventoId = tipoEventoService.getAllByServicio(servicio)!!.map { it.id }

        return servicioDTO
    }

    @PostMapping("/saveServicio")
    fun save(@RequestBody servicioDTO: GenericItemDto): ServicioDTO {
        var servicio = Servicio(servicioDTO.id, servicioDTO.nombre, empresaService.get(servicioDTO.empresaId)!!)

        servicio = servicioService.save(servicio)

        servicioDTO.listaTipoEventoId.forEach {
            val tipoEvento = tipoEventoService.get(it)!!
            tipoEvento.listaServicio.add(servicio)
            tipoEventoService.save(tipoEvento)
        }
        return servicio.toDTO()
    }

    @DeleteMapping("/deleteServicio/{id}")
    fun delete(@PathVariable("id") id : Long): ServicioDTO {
        val servicio = servicioService.get(id)!!
        servicio.fechaBaja = LocalDate.now()
        return servicioService.save(servicio).toDTO()
    }
}