package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.*
import com.estonianport.agendaza.dto.response.CustomResponse
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.Evento
import com.estonianport.agendaza.service.EventoService
import com.estonianport.agendaza.service.EmpresaService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
     * Obtiene el presupuesto de un evento
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
     * Obtiene todos los eventos de una empresa paginados
     */
    @GetMapping("/empresa/{empresaId}/{pageNumber}")
    fun getAllEventoByEmpresaId(
        @PathVariable empresaId: Long,
        @PathVariable pageNumber: Int
    ): ResponseEntity<CustomResponse<List<EventoDTO>>> {
        val eventos = eventoService.getAllEventoByEmpresaId(empresaId, pageNumber)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Eventos obtenidos correctamente",
                data = eventos
            )
        )
    }

    /**
     * Obtiene la cantidad de eventos de una empresa
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
     * Obtiene eventos de una empresa filtrados por nombre o código
     */
    @GetMapping("/empresa/{empresaId}/buscar/{pageNumber}/{buscar}")
    fun getAllEventoByFilterName(
        @PathVariable empresaId: Long,
        @PathVariable pageNumber: Int,
        @PathVariable buscar: String
    ): ResponseEntity<CustomResponse<List<EventoDTO>>> {
        val eventos = eventoService.getAllEventoByFilterName(empresaId, pageNumber, buscar)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Eventos filtrados obtenidos correctamente",
                data = eventos
            )
        )
    }

    /**
     * Obtiene la cantidad de eventos filtrados
     */
    @GetMapping("/empresa/{empresaId}/buscar/{buscar}/cantidad")
    fun getCantidadEventosFiltrados(
        @PathVariable empresaId: Long,
        @PathVariable buscar: String
    ): ResponseEntity<CustomResponse<Int>> {
        val cantidad = eventoService.cantEventosFiltrados(empresaId, buscar)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Cantidad obtenida correctamente",
                data = cantidad
            )
        )
    }

    /**
     * Obtiene eventos de una empresa en una fecha específica
     */
    @GetMapping("/empresa/{empresaId}/fecha/{fecha}")
    fun getAllEventosByFecha(
        @PathVariable empresaId: Long,
        @PathVariable fecha: String
    ): ResponseEntity<CustomResponse<List<EventoDTO>>> {
        // Guardar fecha en el service para usar en el método
        eventoService.asignarFechaFiltro(fecha)
        val eventos = eventoService.getAllEventosByFecha(empresaService.findById(empresaId))

        return ResponseEntity.ok(
            CustomResponse(
                message = "Eventos de la fecha obtenidos correctamente",
                data = eventos
            )
        )
    }

    /**
     * Obtiene eventos para la agenda/calendario de una empresa
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
     * Obtiene eventos de un usuario/cliente en una empresa
     */
    @PutMapping("/usuario-empresa")
    fun getEventosByUsuarioAndEmpresa(
        @RequestBody usuarioEmpresa: UsuarioEmpresaDTO
    ): ResponseEntity<CustomResponse<List<EventoConUsuarioDTO>>> {
        val eventos = eventoService.getEventosByUsuarioAndEmpresa(
            usuarioEmpresa.usuarioId,
            usuarioEmpresa.empresaId
        )

        return ResponseEntity.ok(
            CustomResponse(
                message = "Eventos del usuario obtenidos correctamente",
                data = eventos
            )
        )
    }

    /**
     * Obtiene la cantidad de eventos de un usuario en una empresa
     */
    @PutMapping("/usuario-empresa/cantidad")
    fun getCantEventosByUsuarioAndEmpresa(
        @RequestBody usuarioEmpresa: UsuarioEmpresaDTO
    ): ResponseEntity<CustomResponse<Int>> {
        val cantidad = eventoService.getCantEventosByUsuarioAndEmpresa(
            usuarioEmpresa.usuarioId,
            usuarioEmpresa.empresaId
        )

        return ResponseEntity.ok(
            CustomResponse(
                message = "Cantidad de eventos obtenida correctamente",
                data = cantidad
            )
        )
    }

    // ==================== TIPOS DE EVENTO ====================

    /**
     * Obtiene todos los tipos de evento de una empresa filtrados por duración
     */
    @GetMapping("/tipos-evento/duracion/{duracion}/{empresaId}")
    fun getAllTipoEventoByEmpresaIdAndDuracion(
        @PathVariable empresaId: Long,
        @PathVariable duracion: String
    ): ResponseEntity<CustomResponse<List<TipoEventoDTO>>> {
        val empresa = empresaService.findById(empresaId)
        val tiposEvento = empresa.listaTipoEvento
            .filter { it.fechaBaja == null && it.duracion.name == duracion }
            .map { it.toDTO() }

        return ResponseEntity.ok(
            CustomResponse(
                message = "Tipos de evento obtenidos correctamente",
                data = tiposEvento
            )
        )
    }

    // ==================== DISPONIBILIDAD ====================

    /**
     * Obtiene los horarios disponibles para una fecha específica
     */
    @PutMapping("/disponibilidad/horarios")
    fun getListaEventoByDiaAndEmpresaId(
        @RequestBody eventoBuscarFecha: EventoBuscarFechaDTO
    ): ResponseEntity<CustomResponse<List<String>>> {
        val horarios = eventoService.getListaEventoByDiaAndEmpresaId(eventoBuscarFecha)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Horarios disponibles obtenidos correctamente",
                data = horarios
            )
        )
    }

    /**
     * Valida si un horario está disponible para un evento
     */
    @PutMapping("/disponibilidad/validar")
    fun getHorarioDisponible(
        @RequestBody eventoBuscarFecha: EventoBuscarFechaDTO
    ): ResponseEntity<CustomResponse<Boolean>> {
        val disponible = eventoService.getHorarioDisponible(eventoBuscarFecha)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Validación completada",
                data = disponible
            )
        )
    }

    // ==================== CRUD ====================

    /**
     * Crea o actualiza un evento
     */
    @PostMapping
    fun saveEvento(
        @RequestBody evento: Evento
    ): ResponseEntity<CustomResponse<Evento>> {
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
     * Actualiza los horarios de un evento
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
     */
    @PutMapping("/{eventoId}/extra")
    fun editEventoExtra(
        @PathVariable eventoId: Long,
        @RequestBody evento: EventoExtraDTO
    ): ResponseEntity<CustomResponse<Long>> {
        evento.id = eventoId
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
     */
    @PutMapping("/{eventoId}/catering")
    fun editEventoCatering(
        @PathVariable eventoId: Long,
        @RequestBody evento: EventoCateringDTO
    ): ResponseEntity<CustomResponse<Long>> {
        evento.id = eventoId
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
     */
    @PutMapping("/{eventoId}/anotaciones")
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
     * Actualiza la cantidad de niños en un evento
     */
    @PutMapping("/{eventoId}/capacidad/ninos")
    fun editEventoCantNinos(
        @PathVariable eventoId: Long,
        @RequestBody cantNinos: Int
    ): ResponseEntity<CustomResponse<Int>> {
        val eventoVer = eventoService.getEventoVer(eventoId)
            ?: throw NotFoundException("Evento no encontrado")

        eventoVer.capacidad.capacidadNinos = cantNinos
        val cantActualizada = eventoService.editEventoCantNinos(eventoVer)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Cantidad de niños actualizada correctamente",
                data = cantActualizada
            )
        )
    }

    /**
     * Actualiza la cantidad de adultos en un evento
     */
    @PutMapping("/{eventoId}/capacidad/adultos")
    fun editEventoCantAdultos(
        @PathVariable eventoId: Long,
        @RequestBody cantAdultos: Int
    ): ResponseEntity<CustomResponse<Int>> {
        val eventoVer = eventoService.getEventoVer(eventoId)
            ?: throw NotFoundException("Evento no encontrado")

        eventoVer.capacidad.capacidadAdultos = cantAdultos
        val cantActualizada = eventoService.editEventoCantAdultos(eventoVer)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Cantidad de adultos actualizada correctamente",
                data = cantActualizada
            )
        )
    }

    /**
     * Actualiza el nombre de un evento
     */
    @PutMapping("/{eventoId}/nombre")
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
     * Actualiza las especificaciones de un evento
     */
    @PutMapping("/empresa/{empresaId}/especificaciones")
    fun recorrerEspecificaciones(
        @PathVariable empresaId: Long,
        @RequestBody evento: EventoReservaDTO
    ): ResponseEntity<CustomResponse<Any>> {
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
     */
    @GetMapping("/{eventoId}/comprobante/pdf")
    fun descargarEvento(
        @PathVariable eventoId: Long
    ): ResponseEntity<ByteArray> {
        eventoService.asignarEventoId(eventoId)
        val archivo = eventoService.descargarEvento(eventoId)

        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"evento_$eventoId.pdf\"")
            .header("Content-Type", "application/pdf")
            .body(archivo)
    }

    /**
     * Descarga el estado de cuenta de un evento en PDF
     */
    @GetMapping("/{eventoId}/estado-cuenta/pdf")
    fun generarEstadoDeCuentaPDF(
        @PathVariable eventoId: Long
    ): ResponseEntity<ByteArray> {
        eventoService.asignarEventoId(eventoId)
        val archivo = eventoService.generarEstadoDeCuentaPDF()

        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"estado_cuenta_$eventoId.pdf\"")
            .header("Content-Type", "application/pdf")
            .body(archivo)
    }

    // ==================== ELIMINAR ====================

    /**
     * Elimina un evento (soft delete)
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
