package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.CodigoEmpresaId
import com.estonianport.agendaza.dto.ExtraDTO
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
    fun get(@PathVariable("id") id: Long): PagoDto {
        return pagoService.get(id)!!.toDTO()
    }

    @PostMapping("/savePago")
    fun save(@RequestBody pagoEmpresaEncargado: PagoEmpresaEncargado): PagoDto {
        val empresa = empresaService.get(pagoEmpresaEncargado.empresaId)!!
        val evento = empresa.listaEvento.find{it.codigo == pagoEmpresaEncargado.pago.codigo}!!
        val encargado = usuarioService.get(pagoEmpresaEncargado.usuarioId)!!
        val pagoDto = pagoEmpresaEncargado.pago

        val pago = Pago(pagoDto.id, pagoDto.monto, pagoDto.medioDePago, LocalDateTime.now(), evento, encargado)

        return pagoService.save(pago).toDTO()
    }

    @DeleteMapping("/deletePago/{id}")
    fun delete(@PathVariable("id") id: Long): ResponseEntity<Pago> {
        pagoService.delete(id)
        return ResponseEntity<Pago>(HttpStatus.OK)
    }

    @GetMapping("/getAllMedioDePago")
    fun getAllMedioDePago(): MutableSet<MedioDePago> {
        return MedioDePago.values().toMutableSet()
    }

    @PutMapping("/getEventoForPago")
    fun getEventoForPago(@RequestBody codigoEmpresaId: CodigoEmpresaId): PagoDto {
        val empresa = empresaService.get(codigoEmpresaId.empresaId)!!
        return pagoService.getEventoForPago(codigoEmpresaId.codigo, empresa)}

        @GetMapping("/getAllPagos/{id}/{pageNumber}")
        fun getAllPagos(@PathVariable("id") id: Long, @PathVariable("pageNumber") pageNumber : Int): List<PagoDto> {
            return pagoService.pagos(id,pageNumber)

        }
        @GetMapping("/getAllPagosFilter/{id}/{pageNumber}/{buscar}")
        fun getAllPagosFilter(@PathVariable("id") id: Long, @PathVariable("pageNumber") pageNumber : Int, @PathVariable("buscar") buscar : String): List<PagoDto> {
            return pagoService.pagosFiltrados(id, pageNumber, buscar)

        }
        @GetMapping("/cantPagos/{id}")
        fun cantPagos(@PathVariable("id") id: Long) =  pagoService.contadorDePagos(id)


        @GetMapping("/cantPagosFiltrados/{id}/{buscar}")
        fun cantPagosFiltrados(@PathVariable("id") id: Long, @PathVariable("buscar") buscar : String) = pagoService.contadorDePagosFiltrados(id,buscar)



}