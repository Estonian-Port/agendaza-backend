package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.*
import com.estonianport.agendaza.dto.response.CustomResponse
import com.estonianport.agendaza.model.enums.TipoCargo
import com.estonianport.agendaza.service.CargoService
import com.estonianport.agendaza.service.UsuarioService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/cargos")
@CrossOrigin("*")
class CargoController(
    private val cargoService: CargoService,
    private val usuarioService: UsuarioService
) {

    // ==================== METADATA / ENUMS ====================

    /**
     * Obtiene todos los tipos de cargos disponibles en el sistema (roles)
     */
    @GetMapping("/tipos")
    fun getAllTiposCargo(): ResponseEntity<CustomResponse<Set<TipoCargo>>> {
        val tipos = TipoCargo.entries.toSet()
        return ResponseEntity.ok(
            CustomResponse(
                message = "Tipos de cargo obtenidos correctamente",
                data = tipos
            )
        )
    }

    // ==================== OBTENER CARGOS ====================

    /**
     * Obtiene el cargo de un usuario en una empresa específica
     * @param usuarioId ID del usuario
     * @param empresaId ID de la empresa
     */
    @GetMapping("/{usuarioId}/empresa/{empresaId}")
    fun getCargoByUsuarioAndEmpresa(
        @PathVariable usuarioId: Long,
        @PathVariable empresaId: Long
    ): ResponseEntity<CustomResponse<UsuarioEditCargoDTO>> {
        val cargo = cargoService.getCargoByEmpresaIdAndUsuarioId(empresaId, usuarioId)
        val dto = UsuarioEditCargoDTO(usuarioId, empresaId, cargo.tipoCargo)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Cargo obtenido correctamente",
                data = dto
            )
        )
    }

    /**
     * Obtiene todos los cargos/empleados de una empresa (con paginación)
     * @param empresaId ID de la empresa
     * @param page Número de página (default 0)
     */
    @GetMapping("/empresa/{empresaId}")
    fun getAllCargosByEmpresa(
        @PathVariable empresaId: Long,
        @RequestParam(defaultValue = "0") page: Int
    ): ResponseEntity<CustomResponse<List<UsuarioAbmDTO>>> {
        val cargos = usuarioService.getAllUsuario(empresaId, page)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Cargos obtenidos correctamente",
                data = cargos
            )
        )
    }

    /**
     * Obtiene todos los cargos de un usuario (sus empresas)
     * @param usuarioId ID del usuario
     */
    @GetMapping("/usuario/{usuarioId}")
    fun getAllCargosByUsuario(
        @PathVariable usuarioId: Long
    ): ResponseEntity<CustomResponse<List<AgendaDTO>>> {
        val agendas = cargoService.getListaCargosByUsuarioId(usuarioId)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Agenda obtenida correctamente",
                data = agendas
            )
        )
    }

    /**
     * Obtiene la información completa de un usuario en una empresa (Agenda)
     * Equivalente al viejo `getListaAgendaByUsuarioId`
     * @param usuarioId ID del usuario
     */
    @GetMapping("/usuario/{usuarioId}/agenda")
    fun getAgendaByUsuario(
        @PathVariable usuarioId: Long
    ): ResponseEntity<CustomResponse<List<AgendaDTO>>> {
        val agendas = cargoService.getListaCargosByUsuarioId(usuarioId)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Agenda del usuario obtenida correctamente",
                data = agendas
            )
        )
    }

    // ==================== ACTUALIZAR CARGOS ====================

    /**
     * Actualiza el cargo de un usuario en una empresa
     * @param usuarioId ID del usuario
     * @param dto Datos del cargo a actualizar
     */
    @PutMapping("/{usuarioId}")
    fun updateCargo(
        @PathVariable usuarioId: Long,
        @RequestBody dto: UsuarioEditCargoDTO
    ): ResponseEntity<CustomResponse<Long>> {
        // Mantenemos tu lógica pero usando asignación segura y delegando el comportamiento esperado
        val cargo = cargoService.getCargoByEmpresaIdAndUsuarioId(dto.empresaId, usuarioId)
        cargo.tipoCargo = dto.cargo
        val cargoGuardado = cargoService.save(cargo)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Cargo actualizado correctamente",
                data = cargoGuardado.id
            )
        )
    }

    // ==================== ELIMINAR CARGOS ====================

    /**
     * Elimina el cargo de un usuario en una empresa
     * @param usuarioId ID del usuario
     * @param empresaId ID de la empresa
     */
    @DeleteMapping("/{usuarioId}/empresa/{empresaId}")
    fun deleteCargo(
        @PathVariable usuarioId: Long,
        @PathVariable empresaId: Long
    ): ResponseEntity<CustomResponse<String>> {
        // En el viejo usabas cargoService.delete(empresaId, usuarioId).
        // Si tu nuevo service ahora pide el ID del Cargo, esta lógica que armaste es la correcta:
        val cargo = cargoService.getCargoByEmpresaIdAndUsuarioId(empresaId, usuarioId)
        cargoService.delete(cargo.id)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Cargo eliminado correctamente",
                data = "OK"
            )
        )
    }
}