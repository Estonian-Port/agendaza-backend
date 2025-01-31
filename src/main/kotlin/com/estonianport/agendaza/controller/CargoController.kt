package com.estonianport.agendaza.controller

import com.estonianport.agendaza.model.Cargo
import com.estonianport.agendaza.model.TipoCargo
import com.estonianport.agendaza.service.CargoService
import com.estonianport.agendaza.service.UsuarioService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin("*")
class CargoController {

    @Autowired
    lateinit var cargoService: CargoService

    @Autowired
    lateinit var usuarioService: UsuarioService

    @GetMapping("/getCargoByEmpresaAndUsuario/{empresaId}/{usuarioId}")
    fun getCargoByEmpresaIdAndUsuarioId(@PathVariable("empresaId") empresaId: Long, @PathVariable("usuarioId") usuarioId: Long): TipoCargo {
        return cargoService.getTipoCargoByEmpresaIdAndUsuarioId(empresaId, usuarioId)
    }

    @GetMapping("/getAllCargo")
    fun getAllCargo(): MutableSet<TipoCargo> {
        return TipoCargo.values().toMutableSet()
    }

    @GetMapping("/getCargo/{id}")
    fun get(@PathVariable("id") id: Long): Cargo? {
        return cargoService.findById(id)
    }

    @PostMapping("/saveCargo")
    fun save(@RequestBody cargo: Cargo): Cargo {
        return cargoService.save(cargo)
    }

    @DeleteMapping("/deleteCargo/{empresaId}/{usuarioId}")
    fun delete(@PathVariable("empresaId") empresaId: Long, @PathVariable("usuarioId") usuarioId: Long): ResponseEntity<Cargo> {
        cargoService.delete(empresaId, usuarioId)
        return ResponseEntity<Cargo>(HttpStatus.OK)
    }
}