package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.GenericItemDTO
import com.estonianport.agendaza.dto.ServicioDTO
import com.estonianport.agendaza.dto.response.CustomResponse
import com.estonianport.agendaza.service.ServicioService
import com.estonianport.agendaza.service.TipoEventoService
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
@RequestMapping("/v1/servicios")
@CrossOrigin("*")
class ServicioController(
    private val servicioService: ServicioService
) {

    // ==================== OBTENER SERVICIOS ====================

    /**
     * Obtiene un servicio por su ID, incluyendo los tipos de evento asociados
     * @param id ID del servicio
     */
    @GetMapping("/{id}")
    fun getServicio(@PathVariable id: Long): ResponseEntity<CustomResponse<ServicioDTO>> {
        val servicioDTO = servicioService.getServicioConTiposEvento(id)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Servicio obtenido correctamente",
                data = servicioDTO
            )
        )
    }

    /**
     * Obtiene todos los servicios de una empresa con paginación
     * @param empresaId ID de la empresa
     * @param page Número de página (default 0)
     */
    @GetMapping("/empresa/{empresaId}")
    fun getAllServicioByEmpresa(
        @PathVariable empresaId: Long,
        @RequestParam(defaultValue = "0") page: Int
    ): ResponseEntity<CustomResponse<List<ServicioDTO>>> {
        val servicios = servicioService.getAllServicioByEmpresaId(empresaId, page)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Servicios obtenidos correctamente",
                data = servicios
            )
        )
    }

    /**
     * Obtiene la cantidad total de servicios activos de una empresa
     * @param empresaId ID de la empresa
     */
    @GetMapping("/empresa/{empresaId}/cantidad")
    fun getCantidadServicio(
        @PathVariable empresaId: Long
    ): ResponseEntity<CustomResponse<Int>> {
        val cantidad = servicioService.getCantidadServicio(empresaId)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Cantidad de servicios obtenida correctamente",
                data = cantidad
            )
        )
    }

    /**
     * Obtiene servicios de una empresa filtrados por nombre, con paginación
     * @param empresaId ID de la empresa
     * @param buscar Texto a buscar en el nombre
     * @param page Número de página (default 0)
     */
    @GetMapping("/empresa/{empresaId}/filtrar")
    fun getAllServicioFiltrados(
        @PathVariable empresaId: Long,
        @RequestParam buscar: String,
        @RequestParam(defaultValue = "0") page: Int
    ): ResponseEntity<CustomResponse<List<ServicioDTO>>> {
        val servicios = servicioService.getAllServicioFiltradosNombre(empresaId, buscar, page)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Servicios filtrados obtenidos correctamente",
                data = servicios
            )
        )
    }

    /**
     * Obtiene la cantidad de servicios filtrados por nombre de una empresa
     * @param empresaId ID de la empresa
     * @param buscar Texto a buscar en el nombre
     */
    @GetMapping("/empresa/{empresaId}/filtrar/cantidad")
    fun getCantidadServicioFiltrados(
        @PathVariable empresaId: Long,
        @RequestParam buscar: String
    ): ResponseEntity<CustomResponse<Int>> {
        val cantidad = servicioService.getCantidadServicioFiltrados(empresaId, buscar)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Cantidad de servicios filtrados obtenida correctamente",
                data = cantidad
            )
        )
    }

    /**
     * Obtiene servicios disponibles para agregar a una empresa (los que aún no pertenecen a ella)
     * @param empresaId ID de la empresa
     */
    @GetMapping("/empresa/{empresaId}/agregar")
    fun getAllServicioAgregar(
        @PathVariable empresaId: Long
    ): ResponseEntity<CustomResponse<List<ServicioDTO>>> {
        val servicios = servicioService.getAllServicioAgregar(empresaId)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Servicios disponibles para agregar obtenidos correctamente",
                data = servicios
            )
        )
    }

    /**
     * Obtiene todos los servicios asociados a un tipo de evento
     * @param tipoEventoId ID del tipo de evento
     */
    @GetMapping("/tipo-evento/{tipoEventoId}")
    fun getAllServicioByTipoEventoId(
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
    fun saveServicio(
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
    fun deleteServicio(
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