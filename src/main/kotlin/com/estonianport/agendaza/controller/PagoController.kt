package com.estonianport.agendaza.controller

import com.estonianport.agendaza.common.emailService.EmailService
import com.estonianport.agendaza.common.openPDF.PdfService
import com.estonianport.agendaza.dto.EventoPagoDTO
import com.estonianport.agendaza.dto.PagoDTO
import com.estonianport.agendaza.dto.response.CustomResponse
import com.estonianport.agendaza.model.enums.Concepto
import com.estonianport.agendaza.model.enums.MedioDePago
import com.estonianport.agendaza.service.PagoService
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/pagos")
@CrossOrigin("*")
class PagoController(
    private val pagoService: PagoService
) {

    // ==================== METADATA / ENUMS ====================

    /**
     * Obtiene todos los medios de pago disponibles en el sistema
     */
    @GetMapping("/medios-de-pago")
    fun getAllMedioDePago(): ResponseEntity<CustomResponse<Array<MedioDePago>>> {
        val medios = MedioDePago.entries.toTypedArray()
        return ResponseEntity.ok(
            CustomResponse(
                message = "Medios de pago obtenidos correctamente",
                data = medios
            )
        )
    }

    /**
     * Obtiene todos los conceptos de pago disponibles en el sistema
     */
    @GetMapping("/conceptos")
    fun getAllConcepto(): ResponseEntity<CustomResponse<Array<Concepto>>> {
        val conceptos = Concepto.entries.toTypedArray()
        return ResponseEntity.ok(
            CustomResponse(
                message = "Conceptos obtenidos correctamente",
                data = conceptos
            )
        )
    }

    // ==================== OBTENER PAGOS ====================

    /**
     * Obtiene un pago por su ID
     * @param id ID del pago
     */
    @GetMapping("/{id}")
    fun getPago(
        @PathVariable id: Long
    ): ResponseEntity<CustomResponse<PagoDTO>> {
        val pago = pagoService.getPagoDTO(id)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Pago obtenido correctamente",
                data = pago
            )
        )
    }

    /**
     * Obtiene todos los pagos de una empresa con paginación
     * @param empresaId ID de la empresa
     * @param page Número de página (default 0)
     */
    @GetMapping("/empresa/{empresaId}")
    fun getAllPagosByEmpresa(
        @PathVariable empresaId: Long,
        @RequestParam(defaultValue = "0") page: Int
    ): ResponseEntity<CustomResponse<List<PagoDTO>>> {
        val pagos = pagoService.pagos(empresaId, page)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Pagos obtenidos correctamente",
                data = pagos
            )
        )
    }

    /**
     * Obtiene pagos de una empresa filtrados por código de evento o nombre de cliente, con paginación
     * @param empresaId ID de la empresa
     * @param buscar Texto a buscar
     * @param page Número de página (default 0)
     */
    @GetMapping("/empresa/{empresaId}/filtrar")
    fun getAllPagosFiltrados(
        @PathVariable empresaId: Long,
        @RequestParam buscar: String,
        @RequestParam(defaultValue = "0") page: Int
    ): ResponseEntity<CustomResponse<List<PagoDTO>>> {
        val pagos = pagoService.pagosFiltrados(empresaId, page, buscar)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Pagos filtrados obtenidos correctamente",
                data = pagos
            )
        )
    }

    /**
     * Obtiene la cantidad total de pagos activos de una empresa
     * @param empresaId ID de la empresa
     */
    @GetMapping("/empresa/{empresaId}/cantidad")
    fun getCantidadPagos(
        @PathVariable empresaId: Long
    ): ResponseEntity<CustomResponse<Int>> {
        val cantidad = pagoService.contadorDePagos(empresaId)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Cantidad de pagos obtenida correctamente",
                data = cantidad
            )
        )
    }

    /**
     * Obtiene la cantidad de pagos filtrados de una empresa
     * @param empresaId ID de la empresa
     * @param buscar Texto a buscar
     */
    @GetMapping("/empresa/{empresaId}/filtrar/cantidad")
    fun getCantidadPagosFiltrados(
        @PathVariable empresaId: Long,
        @RequestParam buscar: String
    ): ResponseEntity<CustomResponse<Int>> {
        val cantidad = pagoService.contadorDePagosFiltrados(empresaId, buscar)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Cantidad de pagos filtrados obtenida correctamente",
                data = cantidad
            )
        )
    }

    /**
     * Obtiene la información de un evento para cargar el formulario de nuevo pago
     * @param eventoId ID del evento
     */
    @GetMapping("/evento/{eventoId}/datos-para-pago")
    fun getEventoForSavePago(
        @PathVariable eventoId: Long
    ): ResponseEntity<CustomResponse<PagoDTO>> {
        val pago = pagoService.getEventoForSavePago(eventoId)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Datos del evento para pago obtenidos correctamente",
                data = pago
            )
        )
    }

    /**
     * Obtiene la información de un evento para editar sus pagos (incluye presupuesto total)
     * @param eventoId ID del evento
     */
    @GetMapping("/evento/{eventoId}/estado-cuenta")
    fun getEventoForEditEventoPago(
        @PathVariable eventoId: Long
    ): ResponseEntity<CustomResponse<EventoPagoDTO>> {
        val eventoPago = pagoService.getEventoForEditEventoPago(eventoId)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Estado de cuenta del evento obtenido correctamente",
                data = eventoPago
            )
        )
    }

    /**
     * Obtiene todos los pagos de un evento específico
     * @param eventoId ID del evento
     */
    @GetMapping("/evento/{eventoId}")
    fun getAllPagoFromEvento(
        @PathVariable eventoId: Long
    ): ResponseEntity<CustomResponse<List<PagoDTO>>> {
        val pagos = pagoService.getAllPagoFromEvento(eventoId)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Pagos del evento obtenidos correctamente",
                data = pagos
            )
        )
    }

    // ==================== CREAR PAGOS ====================

    /**
     * Crea un nuevo pago para un evento
     * @param pagoDTO Datos del pago a registrar
     */
    @PostMapping
    fun savePago(
        @RequestBody pagoDTO: PagoDTO
    ): ResponseEntity<CustomResponse<PagoDTO>> {
        val pago = pagoService.savePago(pagoDTO)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Pago guardado correctamente",
                data = pago
            )
        )
    }

    // ==================== ELIMINAR PAGOS ====================

    /**
     * Elimina (soft delete) un pago por su ID
     * @param id ID del pago
     */
    @DeleteMapping("/{id}")
    fun deletePago(
        @PathVariable id: Long
    ): ResponseEntity<CustomResponse<String>> {
        pagoService.delete(id)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Pago eliminado correctamente",
                data = "OK"
            )
        )
    }

    // ==================== COMPROBANTES Y EMAIL ====================

    /**
     * Descarga el comprobante PDF de un pago
     * @param id ID del pago
     */
    @GetMapping("/{id}/comprobante")
    fun descargarPago(
        @PathVariable id: Long
    ): ResponseEntity<ByteArray> {
        val pdfBytes = pagoService.generarComprobantePago(id)
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_PDF
        }
        return ResponseEntity.ok()
            .headers(headers)
            .body(pdfBytes)
    }

    /**
     * Envía el comprobante de pago por email al cliente del evento
     * @param pagoId ID del pago
     * @param eventoId ID del evento
     * @param empresaId ID de la empresa
     */
    @GetMapping("/{pagoId}/email/evento/{eventoId}/empresa/{empresaId}")
    fun enviarEmailPago(
        @PathVariable pagoId: Long,
        @PathVariable eventoId: Long,
        @PathVariable empresaId: Long
    ): ResponseEntity<CustomResponse<Boolean>> {
        val enviado = pagoService.enviarEmailPago(pagoId, eventoId, empresaId)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Email de pago enviado correctamente",
                data = enviado
            )
        )
    }

    /**
     * Descarga el estado de cuenta PDF de un evento
     * @param eventoId ID del evento
     */
    @GetMapping("/evento/{eventoId}/estado-cuenta/pdf")
    fun descargarEstadoCuenta(
        @PathVariable eventoId: Long
    ): ResponseEntity<ByteArray> {
        val pdfBytes = pagoService.generarEstadoCuenta(eventoId)
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_PDF
        }
        return ResponseEntity.ok()
            .headers(headers)
            .body(pdfBytes)
    }

    /**
     * Envía el estado de cuenta por email al cliente del evento
     * @param eventoId ID del evento
     * @param empresaId ID de la empresa
     */
    @GetMapping("/evento/{eventoId}/estado-cuenta/email/empresa/{empresaId}")
    fun enviarEmailEstadoCuenta(
        @PathVariable eventoId: Long,
        @PathVariable empresaId: Long
    ): ResponseEntity<CustomResponse<Boolean>> {
        val enviado = pagoService.enviarEmailEstadoCuenta(eventoId, empresaId)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Email de estado de cuenta enviado correctamente",
                data = enviado
            )
        )
    }
}