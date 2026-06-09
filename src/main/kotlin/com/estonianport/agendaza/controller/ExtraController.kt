package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.ExtraDTO
import com.estonianport.agendaza.dto.ExtraPrecioDTO
import com.estonianport.agendaza.dto.PrecioConFechaDTO
import com.estonianport.agendaza.dto.response.CustomResponse
import com.estonianport.agendaza.model.PrecioConFechaExtra
import com.estonianport.agendaza.model.enums.TipoExtra
import com.estonianport.agendaza.service.EmpresaService
import com.estonianport.agendaza.service.ExtraService
import com.estonianport.agendaza.service.PrecioConFechaExtraService
import com.estonianport.agendaza.service.TipoEventoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.LocalDateTime

@RestController
@RequestMapping("/v1/extras")
@CrossOrigin("*")
class ExtraController(
    private val extraService: ExtraService,
    private val tipoEventoService: TipoEventoService,
    private val empresaService: EmpresaService,
    private val precioConFechaExtraService: PrecioConFechaExtraService
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
     * Obtiene un extra por su ID con sus tipos de evento asociados
     */
    @GetMapping("/{id}")
    fun getExtra(@PathVariable id: Long): ResponseEntity<CustomResponse<ExtraDTO>> {
        val extra = extraService.get(id)!!
        val extraDTO = extra.toDTO().apply {
            listaTipoEventoId = tipoEventoService.getAllByExtra(id).map { it.id }.toMutableSet()
        }
        return ResponseEntity.ok(
            CustomResponse(message = "Extra obtenido correctamente", data = extraDTO)
        )
    }

    /**
     * Lista paginada de extras tipo Evento de una empresa
     */
    @GetMapping("/empresa/{empresaId}/evento")
    fun getPageEvento(
        @PathVariable empresaId: Long,
        @RequestParam(defaultValue = "0") page: Int
    ): ResponseEntity<CustomResponse<List<ExtraDTO>>> =
        ResponseEntity.ok(
            CustomResponse(
                message = "Extras de evento obtenidos correctamente",
                data = extraService.getPageEvento(empresaId, page)
            )
        )

    /**
     * Lista paginada de extras tipo Evento filtrados por nombre
     */
    @GetMapping("/empresa/{empresaId}/evento/buscar")
    fun getPageEventoByNombre(
        @PathVariable empresaId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam buscar: String
    ): ResponseEntity<CustomResponse<List<ExtraDTO>>> =
        ResponseEntity.ok(
            CustomResponse(
                message = "Extras de evento filtrados correctamente",
                data = extraService.getPageEventoByNombre(empresaId, page, buscar)
            )
        )

    /**
     * Cantidad total de extras tipo Evento de una empresa
     */
    @GetMapping("/empresa/{empresaId}/evento/count")
    fun countEvento(@PathVariable empresaId: Long): ResponseEntity<CustomResponse<Int>> =
        ResponseEntity.ok(
            CustomResponse(
                message = "Cantidad de extras de evento obtenida correctamente",
                data = extraService.countEvento(empresaId)
            )
        )

    /**
     * Cantidad de extras tipo Evento filtrados por nombre
     */
    @GetMapping("/empresa/{empresaId}/evento/count/buscar")
    fun countEventoByNombre(
        @PathVariable empresaId: Long,
        @RequestParam buscar: String
    ): ResponseEntity<CustomResponse<Int>> =
        ResponseEntity.ok(
            CustomResponse(
                message = "Cantidad filtrada de extras de evento obtenida correctamente",
                data = extraService.countEventoByNombre(empresaId, buscar)
            )
        )

    /**
     * Lista paginada de extras tipo Catering de una empresa
     */
    @GetMapping("/empresa/{empresaId}/catering")
    fun getPageCatering(
        @PathVariable empresaId: Long,
        @RequestParam(defaultValue = "0") page: Int
    ): ResponseEntity<CustomResponse<List<ExtraDTO>>> =
        ResponseEntity.ok(
            CustomResponse(
                message = "Extras de catering obtenidos correctamente",
                data = extraService.getPageCatering(empresaId, page)
            )
        )

    /**
     * Lista paginada de extras tipo Catering filtrados por nombre
     */
    @GetMapping("/empresa/{empresaId}/catering/buscar")
    fun getPageCateringByNombre(
        @PathVariable empresaId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam buscar: String
    ): ResponseEntity<CustomResponse<List<ExtraDTO>>> =
        ResponseEntity.ok(
            CustomResponse(
                message = "Extras de catering filtrados correctamente",
                data = extraService.getPageCateringByNombre(empresaId, page, buscar)
            )
        )

    /**
     * Cantidad total de extras tipo Catering de una empresa
     */
    @GetMapping("/empresa/{empresaId}/catering/count")
    fun countCatering(@PathVariable empresaId: Long): ResponseEntity<CustomResponse<Int>> =
        ResponseEntity.ok(
            CustomResponse(
                message = "Cantidad de extras de catering obtenida correctamente",
                data = extraService.countCatering(empresaId)
            )
        )

    /**
     * Cantidad de extras tipo Catering filtrados por nombre
     */
    @GetMapping("/empresa/{empresaId}/catering/count/buscar")
    fun countCateringByNombre(
        @PathVariable empresaId: Long,
        @RequestParam buscar: String
    ): ResponseEntity<CustomResponse<Int>> =
        ResponseEntity.ok(
            CustomResponse(
                message = "Cantidad filtrada de extras de catering obtenida correctamente",
                data = extraService.countCateringByNombre(empresaId, buscar)
            )
        )

    /**
     * Todos los extras tipo Evento activos del sistema (sin filtro de empresa)
     */
    @GetMapping("/evento")
    fun getAllEvento(): ResponseEntity<CustomResponse<List<ExtraDTO>>> =
        ResponseEntity.ok(
            CustomResponse(message = "Extras de evento obtenidos correctamente", data = extraService.getAllEvento())
        )

    /**
     * Todos los extras tipo Catering activos del sistema (sin filtro de empresa)
     */
    @GetMapping("/catering")
    fun getAllCatering(): ResponseEntity<CustomResponse<List<ExtraDTO>>> =
        ResponseEntity.ok(
            CustomResponse(message = "Extras de catering obtenidos correctamente", data = extraService.getAllCatering())
        )

    /**
     * Extras tipo Evento que aún no pertenecen a la empresa (para agregar)
     */
    @GetMapping("/empresa/{empresaId}/evento/agregar")
    fun getExtraEventoAgregar(
        @PathVariable empresaId: Long
    ): ResponseEntity<CustomResponse<List<ExtraDTO>>> =
        ResponseEntity.ok(
            CustomResponse(
                message = "Extras de evento disponibles para agregar",
                data = extraService.getAllExtraEventoAgregar(empresaId)
            )
        )

    /**
     * Extras tipo Catering que aún no pertenecen a la empresa (para agregar)
     */
    @GetMapping("/empresa/{empresaId}/catering/agregar")
    fun getExtraCateringAgregar(
        @PathVariable empresaId: Long
    ): ResponseEntity<CustomResponse<List<ExtraDTO>>> =
        ResponseEntity.ok(
            CustomResponse(
                message = "Extras de catering disponibles para agregar",
                data = extraService.getAllExtraCateringAgregar(empresaId)
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