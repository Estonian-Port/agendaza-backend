package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.*
import com.estonianport.agendaza.dto.response.CustomResponse
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.service.EventoService
import com.estonianport.agendaza.service.EmpresaService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/v1/eventos")
@CrossOrigin("*")
class EventoController {

    @Autowired
    lateinit var eventoService: EventoService

    @Autowired
    lateinit var empresaService: EmpresaService

    // ==================== BÚSQUEDAS BÁSICAS ====================

    /**
     * Obtiene un evento por su ID con todos sus detalles
     * GET /v1/eventos/123
     */
    @GetMapping("/{eventoId}")
    fun getEvento(
        @PathVariable eventoId: Long
    ): ResponseEntity<CustomResponse<EventoVerDTO>> {
        val evento = eventoService.getEventoVer(eventoId)
            ?: throw NotFoundException("Evento no encontrado")

        return ResponseEntity.ok(
            CustomResponse(
                message = "Evento obtenido correctamente",
                data = evento
            )
        )
    }

    /**
     * Obtiene el presupuesto total de un evento
     * GET /v1/eventos/123/presupuesto
     */
    @GetMapping("/{eventoId}/presupuesto")
    fun getPresupuesto(
        @PathVariable eventoId: Long
    ): ResponseEntity<CustomResponse<Double>> {
        val presupuesto = eventoService.getPresupuesto(eventoId)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Presupuesto obtenido correctamente",
                data = presupuesto
            )
        )
    }

    /**
     * Obtiene información de extras de un evento
     * GET /v1/eventos/123/extra
     */
    @GetMapping("/{eventoId}/extra")
    fun getEventoExtra(
        @PathVariable eventoId: Long
    ): ResponseEntity<CustomResponse<EventoExtraDTO>> {
        val eventoExtra = eventoService.getEventoExtra(eventoId)
            ?: throw NotFoundException("Evento no encontrado")

        return ResponseEntity.ok(
            CustomResponse(
                message = "Información de extras obtenida correctamente",
                data = eventoExtra
            )
        )
    }

    /**
     * Obtiene información de catering de un evento
     * GET /v1/eventos/123/catering
     */
    @GetMapping("/{eventoId}/catering")
    fun getEventoCatering(
        @PathVariable eventoId: Long
    ): ResponseEntity<CustomResponse<EventoCateringDTO>> {
        val eventoCatering = eventoService.getEventoCatering(eventoId)
            ?: throw NotFoundException("Evento no encontrado")

        return ResponseEntity.ok(
            CustomResponse(
                message = "Información de catering obtenida correctamente",
                data = eventoCatering
            )
        )
    }

    /**
     * Obtiene información de horarios de un evento
     * GET /v1/eventos/123/hora
     */
    @GetMapping("/{eventoId}/hora")
    fun getEventoHora(
        @PathVariable eventoId: Long
    ): ResponseEntity<CustomResponse<EventoHoraDTO>> {
        val eventoHora = eventoService.getEventoHora(eventoId)
            ?: throw NotFoundException("Evento no encontrado")

        return ResponseEntity.ok(
            CustomResponse(
                message = "Información de horarios obtenida correctamente",
                data = eventoHora
            )
        )
    }

    /**
     * Obtiene los estados disponibles para eventos
     * GET /v1/eventos/estados
     */
    @GetMapping("/estados")
    fun getAllEstado(): ResponseEntity<CustomResponse<List<String>>> {
        val estados = eventoService.getAllEstado()

        return ResponseEntity.ok(
            CustomResponse(
                message = "Estados obtenidos correctamente",
                data = estados
            )
        )
    }

    /**
     * Obtiene los estados disponibles para crear nuevos eventos
     * GET /v1/eventos/estados/nuevo
     */
    @GetMapping("/estados/nuevo")
    fun getAllEstadoForSaveEvento(): ResponseEntity<CustomResponse<List<String>>> {
        val estados = eventoService.getAllEstadoForSaveEvento()

        return ResponseEntity.ok(
            CustomResponse(
                message = "Estados para nuevo evento obtenidos correctamente",
                data = estados
            )
        )
    }

    // ==================== EVENTOS POR EMPRESA ====================

    /**
     * Obtiene todos los eventos de una empresa paginados y filtrados
     * GET /v1/eventos/empresa/1/eventos?page=0&size=20&search=nombre
     *
     * @param empresaId ID de la empresa
     * @param page Número de página (0-indexed)
     * @param size Cantidad de registros por página
     * @param search Término de búsqueda opcional (nombre o código)
     */
    @GetMapping("/empresa/{empresaId}/eventos")
    fun getEventosByEmpresa(
        @PathVariable empresaId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<CustomResponse<Map<String, Any>>> {
        val pageable = PageRequest.of(page, size)

        val eventosPage = if (search.isNullOrBlank()) {
            eventoService.getAllEventoByEmpresaId(empresaId, pageable)
        } else {
            eventoService.getAllEventoByFilterName(empresaId, search, pageable)
        }

        val response = mapOf(
            "content" to eventosPage.content,
            "totalElements" to eventosPage.totalElements,
            "totalPages" to eventosPage.totalPages,
            "currentPage" to eventosPage.number,
            "pageSize" to eventosPage.size
        )

        return ResponseEntity.ok(
            CustomResponse(
                message = "Eventos obtenidos correctamente",
                data = response
            )
        )
    }

    /**
     * Obtiene la cantidad total de eventos de una empresa (activos)
     * GET /v1/eventos/empresa/1/cantidad
     */
    @GetMapping("/empresa/{empresaId}/cantidad")
    fun getCantidadEventos(
        @PathVariable empresaId: Long
    ): ResponseEntity<CustomResponse<Int>> {
        val cantidad = eventoService.cantEventos(empresaId)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Cantidad de eventos obtenida correctamente",
                data = cantidad
            )
        )
    }

    /**
     * Obtiene la cantidad de eventos filtrados por búsqueda
     * GET /v1/eventos/empresa/1/cantidad?search=cliente
     */
    @GetMapping("/empresa/{empresaId}/cantidad-filtrada")
    fun getCantidadEventosFiltrados(
        @PathVariable empresaId: Long,
        @RequestParam search: String
    ): ResponseEntity<CustomResponse<Int>> {
        val cantidad = eventoService.cantEventosFiltrados(empresaId, search)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Cantidad obtenida correctamente",
                data = cantidad
            )
        )
    }

    /**
     * Obtiene eventos de una empresa en una fecha específica
     * GET /v1/eventos/empresa/1/por-fecha?fecha=2024-05-27
     */
    @GetMapping("/empresa/{empresaId}/por-fecha")
    fun getAllEventosByFecha(
        @PathVariable empresaId: Long,
        @RequestParam fecha: String
    ): ResponseEntity<CustomResponse<List<EventoDTO>>> {
        val eventos = eventoService.getAllEventosByFecha(fecha, empresaId)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Eventos de la fecha obtenidos correctamente",
                data = eventos
            )
        )
    }

    /**
     * Obtiene eventos para la agenda/calendario de una empresa
     * GET /v1/eventos/empresa/1/agenda
     */
    @GetMapping("/empresa/{empresaId}/agenda")
    fun getAllEventosForAgendaByEmpresaId(
        @PathVariable empresaId: Long
    ): ResponseEntity<CustomResponse<List<EventoAgendaDTO>>> {
        val eventos = eventoService.getAllEventosForAgendaByEmpresaId(empresaId)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Eventos de agenda obtenidos correctamente",
                data = eventos
            )
        )
    }

    // ==================== EVENTOS POR USUARIO ====================

    /**
     * Obtiene eventos contratados por un usuario/cliente en una empresa
     * GET /v1/eventos/usuario/5/empresa/1
     */
    @GetMapping("/usuario/{usuarioId}/empresa/{empresaId}")
    fun getEventosByUsuario(
        @PathVariable usuarioId: Long,
        @PathVariable empresaId: Long
    ): ResponseEntity<CustomResponse<Map<String, Any>>> {
        val eventos = eventoService.getEventosByUsuarioAndEmpresa(usuarioId, empresaId)
        val cantidad = eventoService.getCantEventosByUsuarioAndEmpresa(usuarioId, empresaId)

        val response = mapOf(
            "eventos" to eventos,
            "cantidad" to cantidad
        )

        return ResponseEntity.ok(
            CustomResponse(
                message = "Eventos del usuario obtenidos correctamente",
                data = response
            )
        )
    }

    // ==================== VALIDACIONES ====================

    /**
     * Obtiene la lista de eventos ocupados para un día específico
     * GET /v1/eventos/empresa/{empresaId}/ocupacion-del-dia?fechaEvento=2026-04-05T00:00:00
     */
    @GetMapping("/empresa/{empresaId}/ocupacion-del-dia")
    fun getListaEventoByDiaAndEmpresaId(
        @PathVariable empresaId: Long,
        @RequestParam fechaEvento: LocalDateTime
    ): ResponseEntity<CustomResponse<List<String>>> {

        val listaEventosOcupados = eventoService.getEventosOcupadosDelDia(empresaId, fechaEvento)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Eventos del día obtenidos correctamente",
                data = listaEventosOcupados
            )
        )
    }

    @GetMapping("/disponibilidad/validar")
    fun validarHorarioDisponible(
        @RequestParam empresaId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) desde: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) hasta: LocalDateTime
    ): ResponseEntity<CustomResponse<Boolean>> {
        val disponible = eventoService.getHorarioDisponible(empresaId, desde, hasta)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Validación completada",
                data = disponible
            )
        )
    }

    // ==================== CRUD ====================

    /**
     * Crea un nuevo evento
     * POST /v1/eventos
     */
    @PostMapping
    fun saveEvento(
        @RequestBody evento: EventoReservaDTO
    ): ResponseEntity<CustomResponse<Long>> {
        val eventoGuardado = eventoService.registrarReserva(evento)

        return ResponseEntity.status(HttpStatus.CREATED).body(
            CustomResponse(
                message = "Evento guardado correctamente",
                data = eventoGuardado
            )
        )
    }

    // ==================== ACTUALIZAR INFORMACIÓN ====================

    /**
     * Actualiza los horarios (inicio y fin) de un evento
     * PUT /v1/eventos/123/hora
     */
    @PutMapping("/{eventoId}/hora")
    fun editEventoHora(
        @PathVariable eventoId: Long,
        @RequestBody evento: EventoHoraDTO
    ): ResponseEntity<CustomResponse<EventoHoraDTO>> {
        evento.id = eventoId
        val eventoActualizado = eventoService.editEventoHora(evento)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Horarios actualizados correctamente",
                data = eventoActualizado
            )
        )
    }

    /**
     * Actualiza los extras de un evento
     * PUT /v1/eventos/extra
     */
    @PutMapping("/extra")
    fun editEventoExtra(
        @RequestBody evento: EventoExtraDTO
    ): ResponseEntity<CustomResponse<Long>> {
        val eventoActualizado = eventoService.editEventoExtra(evento)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Extras actualizados correctamente",
                data = eventoActualizado
            )
        )
    }

    /**
     * Actualiza la información de catering de un evento
     * PUT /v1/eventos/catering
     */
    @PutMapping("catering")
    fun editEventoCatering(
        @RequestBody evento: EventoCateringDTO
    ): ResponseEntity<CustomResponse<Long>> {
        val eventoActualizado = eventoService.editEventoCatering(evento)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Catering actualizado correctamente",
                data = eventoActualizado
            )
        )
    }

    /**
     * Actualiza las anotaciones de un evento
     * PATCH /v1/eventos/123/anotaciones
     */
    @PatchMapping("/{eventoId}/anotaciones")
    fun editEventoAnotaciones(
        @PathVariable eventoId: Long,
        @RequestBody anotacion: String
    ): ResponseEntity<CustomResponse<String>> {
        val anotacionActualizada = eventoService.editEventoAnotaciones(anotacion, eventoId)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Anotaciones actualizadas correctamente",
                data = anotacionActualizada
            )
        )
    }

    /**
     * Actualiza la capacidad de un evento (adultos y niños)
     * PATCH /v1/eventos/123/capacidad
     */
    @PatchMapping("/{eventoId}/capacidad")
    fun editEventoCapacidad(
        @PathVariable eventoId: Long,
        @RequestBody capacidad: EventoCapacidadDTO
    ): ResponseEntity<CustomResponse<EventoCapacidadDTO>> {
        val capacidadActualizada = eventoService.editEventoCapacidad(eventoId, capacidad)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Capacidad actualizada correctamente",
                data = capacidadActualizada
            )
        )
    }

    /**
     * Actualiza el nombre de un evento
     * PATCH /v1/eventos/123/nombre
     */
    @PatchMapping("/{eventoId}/nombre")
    fun editEventoNombre(
        @PathVariable eventoId: Long,
        @RequestBody nombre: String
    ): ResponseEntity<CustomResponse<String>> {
        val nombreActualizado = eventoService.editEventoNombre(nombre, eventoId)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Nombre actualizado correctamente",
                data = nombreActualizado
            )
        )
    }

    /**
     * Procesa y retorna especificaciones calculadas de un evento
     * POST /v1/eventos/empresa/1/especificaciones
     *
     * Esta operación es idempotente pero modifica los datos entrada,
     * por eso usamos POST en lugar de GET
     */
    @PostMapping("/empresa/{empresaId}/especificaciones")
    fun procesarEspecificaciones(
        @PathVariable empresaId: Long,
        @RequestBody evento: EventoReservaDTO
    ): ResponseEntity<CustomResponse<EventoReservaDTO>> {
        val especificaciones = eventoService.recorrerEspecificaciones(evento, empresaId)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Especificaciones procesadas correctamente",
                data = especificaciones
            )
        )
    }

    // ==================== COMUNICACIÓN ====================

    /**
     * Reenvia el email confirmatorio de un evento al cliente
     * POST /v1/eventos/123/reenviar-mail?empresaId=1
     */
    @PostMapping("/{eventoId}/reenviar-mail")
    fun reenviarMail(
        @PathVariable eventoId: Long,
        @RequestParam empresaId: Long
    ): ResponseEntity<CustomResponse<String>> {
        val resultado = eventoService.reenviarMail(eventoId, empresaId)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Email reenviado correctamente",
                data = if (resultado) "OK" else "FALLO"
            )
        )
    }

    // ==================== DESCARGAS ====================

    /**
     * Descarga el comprobante/presupuesto de un evento en PDF
     * GET /v1/eventos/123/comprobante/pdf
     */
    @GetMapping("/{eventoId}/comprobante/pdf")
    fun descargarEvento(
        @PathVariable eventoId: Long
    ): ResponseEntity<ByteArray> {
        val archivo = eventoService.descargarEvento(eventoId)

        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"evento_$eventoId.pdf\"")
            .header("Content-Type", "application/pdf")
            .body(archivo)
    }

    /**
     * Descarga el estado de cuenta de un evento en PDF
     * GET /v1/eventos/123/estado-cuenta/pdf
     */
    @GetMapping("/{eventoId}/estado-cuenta/pdf")
    fun generarEstadoDeCuentaPDF(
        @PathVariable eventoId: Long
    ): ResponseEntity<ByteArray> {
        val archivo = eventoService.generarEstadoDeCuentaPDF(eventoId)

        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"estado_cuenta_$eventoId.pdf\"")
            .header("Content-Type", "application/pdf")
            .body(archivo)
    }

    // ==================== ELIMINAR ====================

    /**
     * Elimina un evento (soft delete)
     * DELETE /v1/eventos/123
     */
    @DeleteMapping("/{eventoId}")
    fun deleteEvento(
        @PathVariable eventoId: Long
    ): ResponseEntity<CustomResponse<String>> {
        eventoService.delete(eventoId)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Evento eliminado correctamente",
                data = "OK"
            )
        )
    }
}