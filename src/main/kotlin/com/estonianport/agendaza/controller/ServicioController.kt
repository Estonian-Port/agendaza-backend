package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.GenericItemDTO
import com.estonianport.agendaza.dto.ServicioDTO
import com.estonianport.agendaza.dto.response.CustomResponse
import com.estonianport.agendaza.service.ServicioService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/servicios")
@CrossOrigin("*")
class ServicioController(
    private val servicioService: ServicioService
) {

    // ==================== OBTENER SERVICIOS ====================

    /**
     * Obtiene un servicio por su ID y empresa, incluyendo los tipos de evento asociados
     * @param id ID del servicio
     * @param empresaId ID de la empresa
     */
    @GetMapping("/{id}/empresa/{empresaId}")
    fun getServicio(
        @PathVariable id: Long,
        @PathVariable empresaId: Long
    ): ResponseEntity<CustomResponse<ServicioDTO>> {
        val servicioDTO = servicioService.getServicioConTiposEvento(id, empresaId)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Servicio obtenido correctamente",
                data = servicioDTO
            )
        )
    }

    /**
     * Obtiene los servicios de una empresa con paginación y filtro opcional por nombre
     * @param empresaId ID de la empresa
     * @param page Número de página (default 0)
     * @param buscar Texto opcional a buscar en el nombre
     */
    @GetMapping("/empresa/{empresaId}")
    fun getPage(
        @PathVariable empresaId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(required = false) buscar: String?
    ): ResponseEntity<CustomResponse<List<ServicioDTO>>> {
        val servicios = servicioService.getPage(empresaId, page, buscar)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Servicios obtenidos correctamente",
                data = servicios
            )
        )
    }

    /**
     * Obtiene la cantidad total de servicios activos de una empresa, opcionalmente filtrados por nombre
     * @param empresaId ID de la empresa
     * @param buscar Texto opcional a buscar en el nombre
     */
    @GetMapping("/empresa/{empresaId}/count")
    fun count(
        @PathVariable empresaId: Long,
        @RequestParam(required = false) buscar: String?
    ): ResponseEntity<CustomResponse<Int>> {
        val cantidad = servicioService.count(empresaId, buscar)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Cantidad de servicios obtenida correctamente",
                data = cantidad
            )
        )
    }

    /**
     * Busca servicios disponibles para agregar a una empresa según un término de búsqueda
     * @param empresaId ID de la empresa
     * @param q Texto a buscar en el nombre
     */
    @GetMapping("/empresa/{empresaId}/buscar")
    fun buscar(
        @PathVariable empresaId: Long,
        @RequestParam(defaultValue = "") q: String
    ): ResponseEntity<CustomResponse<List<ServicioDTO>>> {
        val servicios = servicioService.buscarParaAgregar(empresaId, q)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Servicios encontrados",
                data = servicios
            )
        )
    }

    /**
     * Obtiene todos los servicios asociados a un tipo de evento
     * @param tipoEventoId ID del tipo de evento
     */
    @GetMapping("/tipo-evento/{tipoEventoId}")
    fun getAllByTipoEvento(
        @PathVariable tipoEventoId: Long
    ): ResponseEntity<CustomResponse<List<ServicioDTO>>> {
        val servicios = servicioService.getAllServicioByTipoEventoId(tipoEventoId)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Servicios del tipo de evento obtenidos correctamente",
                data = servicios
            )
        )
    }

    // ==================== CREAR / ACTUALIZAR SERVICIOS ====================

    /**
     * Crea o actualiza un servicio, asociándolo a una empresa y tipos de evento
     * @param dto Datos del servicio a guardar
     */
    @PostMapping
    fun save(
        @RequestBody dto: GenericItemDTO
    ): ResponseEntity<CustomResponse<ServicioDTO>> {
        val servicio = servicioService.saveServicio(dto)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Servicio guardado correctamente",
                data = servicio
            )
        )
    }

    // ==================== ELIMINAR SERVICIOS ====================

    /**
     * Elimina (soft delete) un servicio de una empresa
     * @param servicioId ID del servicio
     * @param empresaId ID de la empresa
     */
    @DeleteMapping("/{servicioId}/empresa/{empresaId}")
    fun delete(
        @PathVariable servicioId: Long,
        @PathVariable empresaId: Long
    ): ResponseEntity<CustomResponse<String>> {
        servicioService.deleteService(servicioId, empresaId)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Servicio eliminado correctamente",
                data = "OK"
            )
        )
    }
}