package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.*
import com.estonianport.agendaza.dto.response.CustomResponse
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.service.EmpresaService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/empresas")
@CrossOrigin("*")
class EmpresaController(
    private val empresaService: EmpresaService
){

    // ==================== BÚSQUEDAS ====================

    /**
     * Obtiene una empresa por su ID
     */
    @GetMapping("/{empresaId}")
    fun getEmpresa(
        @PathVariable empresaId: Long
    ): ResponseEntity<CustomResponse<EmpresaDTO>> {
        return ResponseEntity.ok(
            CustomResponse(
                message = "Empresa obtenida correctamente",
                data = empresaService.getEmpresaDTO(empresaId)
            )
        )
    }

    // ==================== INFORMACIÓN ESPECÍFICA ====================

    /**
     * Obtiene las especificaciones de una empresa
     */
    @GetMapping("/{empresaId}/especificaciones")
    fun getEspecificaciones(
        @PathVariable empresaId: Long
    ): ResponseEntity<CustomResponse<List<EspecificacionDTO>>> {
        val especificaciones = empresaService.getEspecificaciones(empresaId)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Especificaciones obtenidas correctamente",
                data = especificaciones
            )
        )
    }

    /**
     * Obtiene estadísticas del panel administrativo de una empresa
     */
    @GetMapping("/{empresaId}/panel-admin/cantidades")
    fun getAllCantidadesForPanelAdmin(
        @PathVariable empresaId: Long
    ): ResponseEntity<CustomResponse<CantidadesPanelAdminDTO>> {
        val cantidades = empresaService.getAllCantidadesForPanelAdminByEmpresaId(empresaId)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Cantidades del panel administrativo obtenidas correctamente",
                data = cantidades
            )
        )
    }

    // ==================== EVENTOS ====================

    /**
     * Obtiene todos los eventos de una empresa paginados
     */
    @GetMapping("/{empresaId}/eventos/{pageNumber}")
    fun getAllEventoByEmpresaId(
        @PathVariable empresaId: Long,
        @PathVariable pageNumber: Int
    ): ResponseEntity<CustomResponse<List<EventoDTO>>> {
        val eventos = empresaService.getAllEventoByEmpresaId(empresaId, pageNumber)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Eventos obtenidos correctamente",
                data = eventos
            )
        )
    }

    /**
     * Obtiene eventos de una empresa filtrados por nombre paginados
     */
    @GetMapping("/{empresaId}/eventos/{pageNumber}/{buscar}")
    fun getAllEventoByFilterName(
        @PathVariable empresaId: Long,
        @PathVariable pageNumber: Int,
        @PathVariable buscar: String
    ): ResponseEntity<CustomResponse<List<EventoDTO>>> {
        val eventos = empresaService.getAllEventoByFilterName(empresaId, pageNumber, buscar)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Eventos filtrados obtenidos correctamente",
                data = eventos
            )
        )
    }

    // ==================== TIPOS DE EVENTO ====================

    /**
     * Obtiene todos los tipos de evento de una empresa filtrados por duración
     * GET /v1/empresas/{empresaId}/tipos-evento?duracion=CORTA
     */
    @GetMapping("/{empresaId}/tipos-evento")
    fun getAllTipoEventoByEmpresaIdAndDuracion(
        @PathVariable empresaId: Long,
        @RequestParam duracion: String
    ): ResponseEntity<CustomResponse<List<TipoEventoDTO>>> {

        // Toda la lógica y el acceso a datos se delegó al service
        val tiposEvento = empresaService.getTiposEventoByDuracion(empresaId, duracion)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Tipos de evento obtenidos correctamente",
                data = tiposEvento
            )
        )
    }

    // ==================== PRECIOS ====================

    /**
     * Obtiene los precios con fecha para un extra específico
     */
    @GetMapping("/{empresaId}/extra/{extraId}/precios-con-fecha")
    fun getAllPrecioConFechaByExtraId(
        @PathVariable empresaId: Long,
        @PathVariable extraId: Long
    ): ResponseEntity<CustomResponse<List<PrecioConFechaDTO>>> {
        val precios = empresaService.getAllPrecioConFechaByExtraId(empresaId, extraId)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Precios obtenidos correctamente",
                data = precios
            )
        )
    }

    /**
     * Obtiene los precios con fecha para un tipo de evento específico
     */
    @GetMapping("/{empresaId}/tipo-evento/{tipoEventoId}/precios-con-fecha")
    fun getAllPrecioConFechaByTipoEvento(
        @PathVariable empresaId: Long,
        @PathVariable tipoEventoId: Long
    ): ResponseEntity<CustomResponse<List<PrecioConFechaDTO>>> {
        val precios = empresaService.getAllPrecioConFechaByTipoEvento(empresaId, tipoEventoId)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Precios obtenidos correctamente",
                data = precios
            )
        )
    }

    // ==================== CRUD ====================

    /**
     * Crea o actualiza una empresa
     */
    @PostMapping
    fun saveEmpresa(
        @RequestBody empresaDTO: EmpresaDTO
    ): ResponseEntity<CustomResponse<GenericItemDTO>> {
        val resultado = empresaService.save(empresaDTO)

        return ResponseEntity.status(HttpStatus.CREATED).body(
            CustomResponse(
                message = "Empresa guardada correctamente",
                data = resultado
            )
        )
    }

    // ==================== ACTUALIZAR ====================

    /**
     * Actualiza información básica de una empresa
     */
    @PutMapping("/{empresaId}")
    fun updateEmpresa(
        @PathVariable empresaId: Long,
        @RequestBody empresaDTO: EmpresaDTO
    ): ResponseEntity<CustomResponse<GenericItemDTO>> {
        empresaDTO.id = empresaId
        val resultado = empresaService.save(empresaDTO)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Empresa actualizada correctamente",
                data = resultado
            )
        )
    }

    // ==================== ELIMINAR ====================

    /**
     * Elimina una empresa (soft delete)
     */
    @DeleteMapping("/{empresaId}")
    fun deleteEmpresa(
        @PathVariable empresaId: Long
    ): ResponseEntity<CustomResponse<String>> {
        val empresa = empresaService.get(empresaId)
            ?: throw NotFoundException("Empresa no encontrada")

        empresaService.delete(empresa.id)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Empresa eliminada correctamente",
                data = "OK"
            )
        )
    }
}
