package com.estonianport.agendaza.controller

import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.PrecioConFecha
import com.estonianport.agendaza.service.ExtraService
import com.estonianport.agendaza.service.PrecioConFechaService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin("*")
class PrecioConFechaController {

    @Autowired
    lateinit var precioConFechaService: PrecioConFechaService

    @GetMapping("/getAllPrecioConFecha")
    fun abm(): MutableList<PrecioConFecha>? {
        return precioConFechaService.getAll()
    }

    @GetMapping("/getPrecioConFecha/{id}")
    fun showSave(@PathVariable("id") id: Long): ResponseEntity<PrecioConFecha>? {
        if (id != 0L) {
            ResponseEntity<PrecioConFecha>(precioConFechaService.get(id), HttpStatus.OK)
        }
        return ResponseEntity<PrecioConFecha>(HttpStatus.NO_CONTENT)
    }

    @PostMapping("/savePrecioConFecha")
    fun save(precioConFecha: PrecioConFecha): ResponseEntity<PrecioConFecha> {
        return ResponseEntity<PrecioConFecha>(precioConFechaService.save(precioConFecha), HttpStatus.OK)
    }

    @GetMapping("/deletePrecioConFecha/{id}")
    fun delete(@PathVariable("id") id: Long): ResponseEntity<PrecioConFecha> {
        precioConFechaService.delete(id)
        return ResponseEntity<PrecioConFecha>(HttpStatus.OK)
    }
}