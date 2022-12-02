package com.estonianport.agendaza.controller

import com.estonianport.agendaza.model.Servicio
import com.estonianport.agendaza.service.ServicioService
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
class ServicioController {

    @Autowired
    lateinit var servicioService: ServicioService

    @GetMapping("/getAllServicio")
    fun getAll(): MutableList<Servicio>? {
        return servicioService.getAll()
    }

    @GetMapping("/getServicio/{id}")
    fun get(@PathVariable("id") id: Long): ResponseEntity<Servicio>? {
        if (id != 0L) {
            ResponseEntity<Servicio>(servicioService.get(id), HttpStatus.OK)
        }
        return ResponseEntity<Servicio>(HttpStatus.NO_CONTENT)
    }

    @PostMapping("/saveServicio")
    fun save(@RequestBody servicio: Servicio): ResponseEntity<Servicio>  {
        return ResponseEntity<Servicio>(servicioService.save(servicio), HttpStatus.OK)
    }

    @DeleteMapping("/deleteServicio/{id}")
    fun delete(@PathVariable("id") id: Long): ResponseEntity<Servicio> {
        servicioService.delete(id)
        return ResponseEntity<Servicio>(HttpStatus.OK)
    }
}