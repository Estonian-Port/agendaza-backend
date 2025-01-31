package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.GenericItemDTO
import com.estonianport.agendaza.dto.ServicioDTO
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.Pago
import com.estonianport.agendaza.model.Servicio
import com.estonianport.agendaza.service.EmpresaService
import com.estonianport.agendaza.service.ServicioService
import com.estonianport.agendaza.service.TipoEventoService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
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
    lateinit var tipoEventoService: TipoEventoService

    @Autowired
    lateinit var empresaService: EmpresaService

    @GetMapping("/getServicio/{id}")
    fun get(@PathVariable("id") id: Long): ServicioDTO {
        val servicio = servicioService.get(id)!!
        val servicioDTO = servicio.toDTO()

        servicioDTO.listaTipoEventoId = tipoEventoService.getAllByServicio(servicioDTO.id).map { it.id }
        return servicioDTO
    }

    @PostMapping("/saveServicio")
    fun save(@RequestBody servicioDTO: GenericItemDTO): ServicioDTO {

        var servicio = Servicio(servicioDTO.id, servicioDTO.nombre)

        servicio.listaTipoEvento = servicioDTO.listaTipoEventoId.map { tipoEventoService.get(it)!! }.toMutableSet()

        servicio = servicioService.save(servicio)

        val empresa = empresaService.findById(servicioDTO.empresaId)
        empresa.listaServicio.add(servicio)

        empresaService.save(empresa)

        return servicio.toDTO()
    }

    @DeleteMapping("/deleteServicio/{id}")
    fun delete(@PathVariable("id") id: Long): ResponseEntity<Servicio> {
        servicioService.deleteService(id)
        return ResponseEntity<Servicio>(HttpStatus.OK)
    }

    @GetMapping("/getAllServicio/{empresaId}/{pageNumber}")
    fun getAllServicioByEmpresaId(@PathVariable("empresaId") empresaId: Long, @PathVariable("pageNumber") pageNumber : Int): List<ServicioDTO> {
        return servicioService.getAllServicioByEmpresaId(empresaId, pageNumber)
    }

    @GetMapping("/getCantidadServicio/{empresaId}")
    fun getCantidadServicio(@PathVariable("empresaId") empresaId: Long): Int {
        return servicioService.getCantidadServicio(empresaId)
    }

    @GetMapping("/getAllServicioFiltrados/{empresaId}/{pageNumber}/{buscar}")
    fun getAllServicioFilterNombre(@PathVariable("empresaId") empresaId: Long, @PathVariable("pageNumber") pageNumber : Int, @PathVariable("buscar") buscar: String): List<ServicioDTO> {
        return servicioService.getAllServicioFilterNombre(empresaId, buscar, pageNumber)
    }

    @GetMapping("/getCantidadServicioFiltrados/{empresaId}/{buscar}")
    fun getCantidadServicioFiltrados(@PathVariable("empresaId") empresaId: Long, @PathVariable("buscar") buscar: String): Int {
        return servicioService.getCantidadServicioFiltrados(empresaId, buscar)
    }
}