package com.estonianport.agendaza.controller

import com.estonianport.agendaza.common.emailService.EmailService
import com.estonianport.agendaza.common.openPDF.PdfService
import com.estonianport.agendaza.dto.CodigoEmpresaId
import com.estonianport.agendaza.dto.EventoPagoDTO
import com.estonianport.agendaza.dto.PagoDTO
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.enums.MedioDePago
import com.estonianport.agendaza.model.Pago
import com.estonianport.agendaza.model.enums.Concepto
import com.estonianport.agendaza.service.EmpresaService
import com.estonianport.agendaza.service.EventoService
import com.estonianport.agendaza.service.PagoService
import com.estonianport.agendaza.service.UsuarioService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
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
import kotlin.coroutines.Continuation

@RestController
@CrossOrigin("*")
class PagoController {

    @Autowired
    lateinit var pagoService: PagoService

    @Autowired
    lateinit var empresaService: EmpresaService

    @Autowired
    lateinit var usuarioService: UsuarioService

    @Autowired
    lateinit var pdfService: PdfService

    @Autowired
    lateinit var eventoService: EventoService

    @Autowired
    lateinit var emailService: EmailService


    @GetMapping("/getPago/{id}")
    fun get(@PathVariable("id") id: Long): PagoDTO {
        return pagoService.get(id)!!.toDTO()
    }

    @PostMapping("/savePago")
    fun save(@RequestBody pagoDTO: PagoDTO): PagoDTO {
        val evento = eventoService.getByCodigoAndEmpresaId(pagoDTO.codigo, pagoDTO.empresaId)
        val encargado = usuarioService.get(pagoDTO.usuarioId)!!

        val pago = pagoService.fromDTO(pagoDTO, evento, encargado)
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

    @GetMapping("/getAllConcepto")
    fun getAllConcepto(): MutableSet<Concepto> {
        return Concepto.values().toMutableSet()
    }

    @PutMapping("/getEventoForPago")
    fun getEventoForPago(@RequestBody codigoEmpresaId: CodigoEmpresaId): PagoDTO {
        val empresa = empresaService.get(codigoEmpresaId.empresaId)!!
        return pagoService.getEventoForPago(codigoEmpresaId.codigo, empresa)
    }

    @GetMapping("/getAllPagos/{id}/{pageNumber}")
    fun getAllPagos(@PathVariable("id") id: Long, @PathVariable("pageNumber") pageNumber : Int): List<PagoDTO> {
        return pagoService.pagos(id,pageNumber)

    }
    @GetMapping("/getAllPagosFilter/{id}/{pageNumber}/{buscar}")
    fun getAllPagosFilter(@PathVariable("id") id: Long, @PathVariable("pageNumber") pageNumber : Int, @PathVariable("buscar") buscar : String): List<PagoDTO> {
        return pagoService.pagosFiltrados(id, pageNumber, buscar)

    }
    @GetMapping("/cantPagos/{id}")
    fun cantPagos(@PathVariable("id") id: Long) =  pagoService.contadorDePagos(id)


    @GetMapping("/cantPagosFiltrados/{id}/{buscar}")
    fun cantPagosFiltrados(@PathVariable("id") id: Long, @PathVariable("buscar") buscar : String) : Int {
        return pagoService.contadorDePagosFiltrados(id,buscar)
    }

    // TODO se podria dividir en dos consultas, una q traiga a lista y otra q traiga la info del evento
    @GetMapping("/getAllPagoFromEvento/{eventoId}")
    fun getAllPagoFromEvento(@PathVariable("eventoId") eventoId: Long): EventoPagoDTO {
        return eventoService.get(eventoId)!!.toEventoPagoDto(pagoService.getAllPagoFromEvento(eventoId))
    }

    @GetMapping("/descargarPago/{id}")
    fun descargarPago(@PathVariable("id") id: Long): ResponseEntity<ByteArray> {
        val pago = pagoService.get(id)!!
        val pdfBytes = pdfService.generarComprobanteDePago(pago)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_PDF
        return ResponseEntity(pdfBytes, headers, HttpStatus.OK)
    }

    @GetMapping("/enviarEmailPago/{pago_id}/{evento_id}/{empresa_id}")
    fun enviarEmailPago(@PathVariable("pago_id") pagoId: Long,@PathVariable("evento_id") eventoId: Long, @PathVariable("empresa_id") empresaId: Long): Boolean {

        try {
            val pago = pagoService.get(pagoId)!!
            val evento = eventoService.get(eventoId)!!
            val empresa = empresaService.get(empresaId)!!

            emailService.enviarEmailPago(pago, evento,empresa)
            return true
        } catch (e: Exception) {
            throw NotFoundException("No se pudo enviar el mail")
        }
    }

    @GetMapping("/descargarEstadoCuenta/{evento_id}")
    fun descargarEstadoCuenta(@PathVariable("evento_id") eventoId: Long): ResponseEntity<ByteArray> {
        val evento = eventoService.get(eventoId)!!
        val pdfBytes = pdfService.generarEstadoDeCuenta(evento)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_PDF
        return ResponseEntity(pdfBytes, headers, HttpStatus.OK)
    }

    @GetMapping("/enviarEmailEstadoCuenta/{evento_id}/{empresa_id}")
    fun enviarEmailEstadoCuenta(@PathVariable("evento_id") eventoId: Long, @PathVariable("empresa_id") empresaId: Long): Boolean {

        try {
            val evento = eventoService.get(eventoId)!!
            val empresa = empresaService.get(empresaId)!!

            emailService.enviarEmailEstadoCuenta(evento,empresa)
            return true
        } catch (e: Exception) {
            throw NotFoundException("No se pudo enviar el mail")
        }
    }
}