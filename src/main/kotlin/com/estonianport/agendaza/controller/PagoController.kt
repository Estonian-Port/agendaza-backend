package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.CodigoEmpresaId
import com.estonianport.agendaza.dto.PagoDto
import com.estonianport.agendaza.dto.PagoEmpresaEncargado
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.MedioDePago
import com.estonianport.agendaza.model.Pago
import com.estonianport.agendaza.service.EmpresaService
import com.estonianport.agendaza.service.PagoService
import com.estonianport.agendaza.service.UsuarioService
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
import java.time.LocalDateTime

@RestController
@CrossOrigin("*")
class PagoController {

    @Autowired
    lateinit var pagoService: PagoService

    @Autowired
    lateinit var empresaService: EmpresaService


    @Autowired
    lateinit var usuarioService: UsuarioService

    @GetMapping("/getPago/{id}")
    fun get(@PathVariable("id") id: Long): ResponseEntity<Pago>? {
        if (id != 0L) {
            ResponseEntity<Pago>(pagoService.get(id), HttpStatus.OK)
        }
        return ResponseEntity<Pago>(HttpStatus.NO_CONTENT)
    }

    @PostMapping("/savePago")
    fun save(@RequestBody pagoEmpresaEncargado: PagoEmpresaEncargado): ResponseEntity<Pago> {
        val empresa = empresaService.get(pagoEmpresaEncargado.empresaId)!!
        val evento = empresa.listaEvento.find{it.codigo == pagoEmpresaEncargado.pago.codigo}!!
        val encargado = usuarioService.get(pagoEmpresaEncargado.usuarioId)!!
        val pagoDto = pagoEmpresaEncargado.pago

        val pago = Pago(pagoDto.id, pagoDto.monto, pagoDto.medioDePago, LocalDateTime.now(), evento, encargado)

        pagoService.save(pago)
        return ResponseEntity<Pago>(HttpStatus.OK)
    }

    @DeleteMapping("/deletePago/{id}")
    fun delete(@PathVariable("id") id: Long): ResponseEntity<Pago> {
        pagoService.delete(id)
        return ResponseEntity<Pago>(HttpStatus.OK)
    }

    @GetMapping("/getAllMedioDePago")
    fun getAllMedioDePago(): ResponseEntity<MutableSet<MedioDePago>>? {
        return ResponseEntity<MutableSet<MedioDePago>>(MedioDePago.values().toMutableSet(),HttpStatus.OK)
    }

    @PutMapping("/getEventoForPago")
    fun getEventoForPago(@RequestBody codigoEmpresaId: CodigoEmpresaId): PagoDto {
        val empresa = empresaService.get(codigoEmpresaId.empresaId)!!
        return pagoService.getEventoForPago(codigoEmpresaId.codigo, empresa)
    }

}