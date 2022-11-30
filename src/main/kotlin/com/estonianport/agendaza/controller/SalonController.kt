package com.estonianport.agendaza.controller

import com.estonianport.agendaza.model.Salon
import com.estonianport.agendaza.service.SalonService
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
class SalonController {

    @Autowired
    lateinit var salonService: SalonService


    @GetMapping("/getAllSalon")
    fun abm(): MutableList<Salon>? {
        return salonService.getAll()
    }

    @GetMapping("/saveSalon/{id}")
    fun showSave(@PathVariable("id") id: Long): ResponseEntity<Salon>? {
        if (id != 0L) {
            return ResponseEntity<Salon>(salonService.get(id), HttpStatus.OK)
        }

        return ResponseEntity<Salon>(HttpStatus.NO_CONTENT)
    }

    @PostMapping("/saveSalon")
    fun save(@RequestBody salon: Salon): ResponseEntity<Salon> {
        return  ResponseEntity<Salon>(salonService.save(salon), HttpStatus.OK)
    }

    @DeleteMapping("/deleteSalon/{id}")
    fun delete(@PathVariable("id") id: Long): ResponseEntity<Salon> {

        //Elimina todos los eventos del salon 
        //val listaEventos: List<Evento> = eventoService.getEventosBySalon(salonService.get(id))
        //for (evento in listaEventos) {
          //  eventoService.delete(evento.getId())
        //}

        //Elmina el salon
        salonService.delete(id)
        return ResponseEntity<Salon>(HttpStatus.OK)
    }
}