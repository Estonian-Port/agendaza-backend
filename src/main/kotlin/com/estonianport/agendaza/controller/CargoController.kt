package com.estonianport.agendaza.controller

import com.estonianport.agendaza.model.Cargo
import com.estonianport.agendaza.service.CargoService
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
class CargoController {

    @Autowired
    lateinit var cargoService: CargoService

    @GetMapping("/getAllCargo")
    fun getAll(): MutableList<Cargo>? {
        return cargoService.getAll()
    }

    @GetMapping("/getCargo/{id}")
    fun get(@PathVariable("id") id: Long): Cargo? {
        return cargoService.findById(id)
    }

    @PostMapping("/saveCargo")
    fun save(@RequestBody cargo: Cargo): Cargo {
        return cargoService.save(cargo)
    }

    @DeleteMapping("/deleteCargo/{id}")
    fun delete(@PathVariable("id") id: Long): ResponseEntity<Cargo> {
        cargoService.delete(id)
        return ResponseEntity<Cargo>(HttpStatus.OK)
    }
}