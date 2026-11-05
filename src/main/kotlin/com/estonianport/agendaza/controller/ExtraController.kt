package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.ExtraDTO
import com.estonianport.agendaza.dto.ExtraPrecioDTO
import com.estonianport.agendaza.dto.PrecioConFechaDTO
import com.estonianport.agendaza.dto.response.CustomResponse
import com.estonianport.agendaza.dto.toDTO
import com.estonianport.agendaza.model.enums.TipoExtra
import com.estonianport.agendaza.service.EmpresaService
import com.estonianport.agendaza.service.ExtraService
import com.estonianport.agendaza.service.TipoEventoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/v1/extras")
@CrossOrigin("*")
class ExtraController(
    private val extraService: ExtraService,
    private val tipoEventoService: TipoEventoService,
    private val empresaService: EmpresaService,
) {

    // ==================== METADATA / ENUMS ====================

    /**
     * Retorna los TipoExtra válidos para el módulo Evento
     */
    @GetMapping("/tipos/evento")
    fun getTiposEvento(): ResponseEntity<CustomResponse<Set<TipoExtra>>> =
        ResponseEntity.ok(
            CustomResponse(
                message = "Tipos de extra de evento obtenidos correctamente",
                data = setOf(TipoExtra.EVENTO, TipoExtra.VARIABLE_EVENTO)
            )
        )

    /**
     * Retorna los TipoExtra válidos para el módulo Catering
     */
    @GetMapping("/tipos/catering")
    fun getTiposCatering(): ResponseEntity<CustomResponse<Set<TipoExtra>>> =
        ResponseEntity.ok(
            CustomResponse(
                message = "Tipos de extra de catering obtenidos correctamente",
                data = setOf(TipoExtra.TIPO_CATERING, TipoExtra.VARIABLE_CATERING)
            )
        )

    // ==================== OBTENER EXTRA ====================

    /**
     * Obtiene un extra por su ID y empresaId con sus tipos de evento asociados
     */
    @GetMapping("/{id}/empresa/{empresaId}")
    fun getExtra(
        @PathVariable empresaId: Long,
        @PathVariable id: Long
    ): ResponseEntity<CustomResponse<ExtraDTO>> {
        val extra = extraService.get(id)!!
        val extraDTO = extra.toDTO().apply {
            listaTipoEventoId = tipoEventoService.getIdsByExtraAndEmpresa(id, empresaId).toMutableSet()
        }
        return ResponseEntity.ok(
            CustomResponse(message = "Extra obtenido correctamente", data = extraDTO)
        )
    }

    /**
     * Lista paginada de extras de una empresa por tipo (opcionalmente filtrados por nombre)
     */
    @GetMapping("/empresa/{empresaId}")
    fun getPage(
        @PathVariable empresaId: Long,
        @RequestParam tiposExtra: List<TipoExtra>,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(required = false) buscar: String?
    ): ResponseEntity<CustomResponse<List<ExtraDTO>>> =
        ResponseEntity.ok(
            CustomResponse(
                message = "Extras obtenidos correctamente",
                data = extraService.getPage(empresaId, tiposExtra, page, buscar)
            )
        )

    /**
     * Cantidad total de extras de una empresa por tipo (opcionalmente filtrados por nombre)
     */
    @GetMapping("/empresa/{empresaId}/count")
    fun count(
        @PathVariable empresaId: Long,
        @RequestParam tiposExtra: List<TipoExtra>,
        @RequestParam(required = false) buscar: String?
    ): ResponseEntity<CustomResponse<Int>> =
        ResponseEntity.ok(
            CustomResponse(
                message = "Cantidad de extras obtenida correctamente",
                data = extraService.count(empresaId, tiposExtra, buscar)
            )
        )

    // ==================== PRECIO ====================

    /**
     * Precios con fechas vigentes de un extra para una empresa
     */
    @GetMapping("/{extraId}/empresa/{empresaId}/precios")
    fun getPreciosByExtra(
        @PathVariable empresaId: Long,
        @PathVariable extraId: Long
    ): ResponseEntity<CustomResponse<List<PrecioConFechaDTO>>> =
        ResponseEntity.ok(
            CustomResponse(
                message = "Precios del extra obtenidos correctamente",
                data = empresaService.getAllPrecioConFechaByExtraId(empresaId, extraId)
            )
        )

    /**
     * Extras con su precio vigente filtrados por tipo de evento, empresa y fecha
     */
    @GetMapping("/empresa/{empresaId}/tipo-evento/{tipoEventoId}/precio")
    fun getExtrasConPrecio(
        @PathVariable empresaId: Long,
        @PathVariable tipoEventoId: Long,
        @RequestParam fechaEvento: LocalDateTime,
        @RequestParam tipoExtra: TipoExtra
    ): ResponseEntity<CustomResponse<List<ExtraPrecioDTO>>> =
        ResponseEntity.ok(
            CustomResponse(
                message = "Extras con precio obtenidos correctamente",
                data = extraService.getAllExtraConPrecioByTipoEventoAndFecha(empresaId, tipoEventoId, fechaEvento, tipoExtra)
            )
        )

    // ==================== ABM ====================

    @GetMapping("/empresa/{empresaId}/buscar")
    fun buscar(
        @PathVariable empresaId: Long,
        @RequestParam(defaultValue = "") q: String,
        @RequestParam tipos: List<TipoExtra>
    ): ResponseEntity<CustomResponse<List<ExtraDTO>>> = ResponseEntity.ok(
        CustomResponse("Extras encontrados", extraService.buscarParaAgregar(empresaId, q, tipos))
    )

    /**
     * Crea o actualiza un extra y lo asocia a la empresa
     */
    @PostMapping
    fun save(@RequestBody extraDTO: ExtraDTO): ResponseEntity<CustomResponse<ExtraDTO>> =
        ResponseEntity.ok(
            CustomResponse(
                message = "Extra guardado correctamente",
                data = extraService.saveExtra(extraDTO, tipoEventoService)
            )
        )

    /**
     * Guarda los precios vigentes de un extra para una empresa.
     * Da de baja lógicamente los que no estén en la nueva lista.
     */
    @PostMapping("/{extraId}/empresa/{empresaId}/precios")
    fun savePrecios(
        @PathVariable empresaId: Long,
        @PathVariable extraId: Long,
        @RequestBody listaPrecioDTO: MutableSet<PrecioConFechaDTO>
    ): ResponseEntity<CustomResponse<String>> {
        extraService.savePreciosConFecha(empresaId, extraId, listaPrecioDTO)
        return ResponseEntity.ok(
            CustomResponse(message = "Precios guardados correctamente", data = "OK")
        )
    }

    /**
     * Elimina (soft-delete) la relación entre un extra y una empresa
     */
    @DeleteMapping("/{extraId}/empresa/{empresaId}")
    fun delete(
        @PathVariable extraId: Long,
        @PathVariable empresaId: Long
    ): ResponseEntity<CustomResponse<String>> {
        extraService.deleteExtra(extraId, empresaId)
        return ResponseEntity.ok(
            CustomResponse(message = "Extra eliminado correctamente", data = "OK")
        )
    }
}