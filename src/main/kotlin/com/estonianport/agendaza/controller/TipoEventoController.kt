package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.EventoCapacidadDTO
import com.estonianport.agendaza.dto.ExtraPrecioDTO
import com.estonianport.agendaza.dto.PrecioConFechaDTO
import com.estonianport.agendaza.dto.TipoEventoDTO
import com.estonianport.agendaza.dto.TimeDTO
import com.estonianport.agendaza.dto.response.CustomResponse
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.enums.Duracion
import com.estonianport.agendaza.model.enums.TipoExtra
import com.estonianport.agendaza.service.TipoEventoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.LocalTime

@RestController
@RequestMapping("/v1/tipos-evento")
@CrossOrigin("*")
class TipoEventoController(
    private val tipoEventoService: TipoEventoService
) {

    // ==================== METADATA / ENUMS ====================

    /**
     * Obtiene todos los tipos de duración disponibles en el sistema
     */
    @GetMapping("/duraciones")
    fun getAllDuracion(): ResponseEntity<CustomResponse<Set<Duracion>>> {
        val duraciones = Duracion.entries.toSet()
        return ResponseEntity.ok(
            CustomResponse(
                message = "Duraciones obtenidas correctamente",
                data = duraciones
            )
        )
    }

    // ==================== OBTENER TIPOS DE EVENTO ====================

    /**
     * Obtiene un tipo de evento por su ID
     * @param id ID del tipo de evento
     */
    @GetMapping("/{id}")
    fun getTipoEvento(
        @PathVariable id: Long
    ): ResponseEntity<CustomResponse<TipoEventoDTO>> {
        val tipoEvento = tipoEventoService.getTipoEventoDTO(id)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Tipo de evento obtenido correctamente",
                data = tipoEvento
            )
        )
    }

    /**
     * Obtiene todos los tipos de evento de una empresa (sin paginación, para selects/combos)
     * @param empresaId ID de la empresa
     */
    @GetMapping("/empresa/{empresaId}/todos")
    fun getAllTipoEventoByEmpresa(
        @PathVariable empresaId: Long
    ): ResponseEntity<CustomResponse<List<TipoEventoDTO>>> {
        val tiposEvento = tipoEventoService.getAllTipoEventoByEmpresaId(empresaId)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Tipos de evento obtenidos correctamente",
                data = tiposEvento
            )
        )
    }

    /**
     * Obtiene tipos de evento de una empresa con paginación
     * @param empresaId ID de la empresa
     * @param page Número de página (default 0)
     */
    @GetMapping("/empresa/{empresaId}")
    fun getAllTipoEventoByEmpresaPaginado(
        @PathVariable empresaId: Long,
        @RequestParam(defaultValue = "0") page: Int
    ): ResponseEntity<CustomResponse<List<TipoEventoDTO>>> {
        val tiposEvento = tipoEventoService.getAllTipoEventoByEmpresaId(empresaId, page)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Tipos de evento obtenidos correctamente",
                data = tiposEvento
            )
        )
    }

    /**
     * Obtiene la cantidad total de tipos de evento activos de una empresa
     * @param empresaId ID de la empresa
     */
    @GetMapping("/empresa/{empresaId}/cantidad")
    fun getCantidadTipoEvento(
        @PathVariable empresaId: Long
    ): ResponseEntity<CustomResponse<Int>> {
        val cantidad = tipoEventoService.getCantidadTipoEvento(empresaId)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Cantidad de tipos de evento obtenida correctamente",
                data = cantidad
            )
        )
    }

    /**
     * Obtiene tipos de evento de una empresa filtrados por nombre, con paginación
     * @param empresaId ID de la empresa
     * @param buscar Texto a buscar en el nombre
     * @param page Número de página (default 0)
     */
    @GetMapping("/empresa/{empresaId}/filtrar")
    fun getAllTipoEventoFiltrados(
        @PathVariable empresaId: Long,
        @RequestParam buscar: String,
        @RequestParam(defaultValue = "0") page: Int
    ): ResponseEntity<CustomResponse<List<TipoEventoDTO>>> {
        val tiposEvento = tipoEventoService.getAllTipoEventoFilterNombre(empresaId, buscar, page)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Tipos de evento filtrados obtenidos correctamente",
                data = tiposEvento
            )
        )
    }

    /**
     * Obtiene la cantidad de tipos de evento filtrados por nombre de una empresa
     * @param empresaId ID de la empresa
     * @param buscar Texto a buscar en el nombre
     */
    @GetMapping("/empresa/{empresaId}/filtrar/cantidad")
    fun getCantidadTipoEventoFiltrados(
        @PathVariable empresaId: Long,
        @RequestParam buscar: String
    ): ResponseEntity<CustomResponse<Int>> {
        val cantidad = tipoEventoService.getCantidadTipoEventoFiltrados(empresaId, buscar)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Cantidad de tipos de evento filtrados obtenida correctamente",
                data = cantidad
            )
        )
    }

    /**
     * Obtiene todos los tipos de evento que contienen un extra específico
     * @param extraId ID del extra
     */
    @GetMapping("/extra/{extraId}")
    fun getListaTipoEventoByExtra(
        @PathVariable extraId: Long
    ): ResponseEntity<CustomResponse<List<TipoEventoDTO>>> {
        val tiposEvento = tipoEventoService.getAllByExtra(extraId)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Tipos de evento del extra obtenidos correctamente",
                data = tiposEvento
            )
        )
    }

    /**
     * Obtiene todos los tipos de evento asociados a un servicio
     * @param servicioId ID del servicio
     */
    @GetMapping("/servicio/{servicioId}")
    fun getListaTipoEventoByServicio(
        @PathVariable servicioId: Long
    ): ResponseEntity<CustomResponse<List<TipoEventoDTO>>> {
        val tiposEvento = tipoEventoService.getAllByServicio(servicioId)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Tipos de evento del servicio obtenidos correctamente",
                data = tiposEvento
            )
        )
    }

    // ==================== DATOS DE TIPO DE EVENTO ====================

    /**
     * Obtiene la duración de un tipo de evento
     * @param id ID del tipo de evento
     */
    @GetMapping("/{id}/duracion")
    fun getDuracionByTipoEventoId(
        @PathVariable id: Long
    ): ResponseEntity<CustomResponse<LocalTime>> {
        val duracion = tipoEventoService.getDuracion(id)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Duración obtenida correctamente",
                data = duracion
            )
        )
    }

    /**
     * Obtiene la capacidad de un tipo de evento
     * @param id ID del tipo de evento
     */
    @GetMapping("/{id}/capacidad")
    fun getCapacidadByTipoEventoId(
        @PathVariable id: Long
    ): ResponseEntity<CustomResponse<EventoCapacidadDTO>> {
        val capacidad = tipoEventoService.getCapacidad(id)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Capacidad obtenida correctamente",
                data = capacidad
            )
        )
    }

    /**
     * Calcula el horario de fin dado un tipo de evento y un horario de inicio
     * @param id ID del tipo de evento
     * @param timeStart Horario de inicio
     */
    @PutMapping("/{id}/hora-fin")
    fun getTimeEndByTipoEventoIdAndTimeStart(
        @PathVariable id: Long,
        @RequestBody timeStart: TimeDTO
    ): ResponseEntity<CustomResponse<LocalTime>> {
        val timeEnd = tipoEventoService.calcularHoraFin(id, timeStart)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Hora de fin calculada correctamente",
                data = timeEnd
            )
        )
    }

    /**
     * Busca el extra de tipo "Niño" de un tipo de evento
     * @param id ID del tipo de evento
     */
    @GetMapping("/{id}/extra-nino")
    fun findExtraNinoByTipoEventoId(
        @PathVariable id: Long
    ): ResponseEntity<CustomResponse<Extra?>> {
        val extra = tipoEventoService.findExtraNino(id)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Extra Niño obtenido correctamente",
                data = extra
            )
        )
    }

    /**
     * Busca el extra de tipo "Camarera" de un tipo de evento
     * @param id ID del tipo de evento
     */
    @GetMapping("/{id}/extra-camarera")
    fun findExtraCamareraByTipoEventoId(
        @PathVariable id: Long
    ): ResponseEntity<CustomResponse<Extra?>> {
        val extra = tipoEventoService.findExtraCamarera(id)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Extra Camarera obtenido correctamente",
                data = extra
            )
        )
    }

    // ==================== PRECIOS ====================

    /**
     * Obtiene todos los precios con fecha de un tipo de evento en una empresa
     * @param empresaId ID de la empresa
     * @param tipoEventoId ID del tipo de evento
     */
    @GetMapping("/{tipoEventoId}/empresa/{empresaId}/precios")
    fun getAllPrecioConFechaByTipoEventoId(
        @PathVariable empresaId: Long,
        @PathVariable tipoEventoId: Long
    ): ResponseEntity<CustomResponse<List<PrecioConFechaDTO>>> {
        val precios = tipoEventoService.getAllPrecioConFecha(empresaId, tipoEventoId)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Precios obtenidos correctamente",
                data = precios
            )
        )
    }

    /**
     * Obtiene el precio de un tipo de evento para una fecha específica
     * @param empresaId ID de la empresa
     * @param tipoEventoId ID del tipo de evento
     * @param fechaEvento Fecha del evento
     */
    @GetMapping("/{tipoEventoId}/empresa/{empresaId}/precio-por-fecha")
    fun getPrecioByTipoEventoIdAndFecha(
        @PathVariable empresaId: Long,
        @PathVariable tipoEventoId: Long,
        @RequestParam fechaEvento: LocalDateTime
    ): ResponseEntity<CustomResponse<Double>> {
        val precio = tipoEventoService.getPrecio(empresaId, tipoEventoId, fechaEvento)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Precio obtenido correctamente",
                data = precio
            )
        )
    }

    /**
     * Guarda o actualiza los precios con fechas de un tipo de evento en una empresa
     * @param empresaId ID de la empresa
     * @param tipoEventoId ID del tipo de evento
     * @param listaPrecioConFechaDTO Lista de precios con fechas a guardar
     */
    @PostMapping("/{tipoEventoId}/empresa/{empresaId}/precios")
    fun saveTipoEventoPrecio(
        @PathVariable empresaId: Long,
        @PathVariable tipoEventoId: Long,
        @RequestBody listaPrecioConFechaDTO: MutableSet<PrecioConFechaDTO>
    ): ResponseEntity<CustomResponse<String>> {
        tipoEventoService.savePreciosConFecha(empresaId, tipoEventoId, listaPrecioConFechaDTO)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Precios guardados correctamente",
                data = "OK"
            )
        )
    }

    // ==================== EXTRAS POR TIPO ====================

    /**
     * Obtiene los extras de tipo EVENTO con precio para un tipo de evento y fecha
     * @param empresaId ID de la empresa
     * @param tipoEventoId ID del tipo de evento
     * @param fechaEvento Fecha del evento
     */
    @PutMapping("/empresa/{empresaId}/{tipoEventoId}/extras/evento")
    fun getAllExtraEventoConPrecio(
        @PathVariable empresaId: Long,
        @PathVariable tipoEventoId: Long,
        @RequestBody fechaEvento: LocalDateTime
    ): ResponseEntity<CustomResponse<List<ExtraPrecioDTO>>> {
        val extras = tipoEventoService.getExtrasConPrecio(empresaId, tipoEventoId, fechaEvento, TipoExtra.EVENTO)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Extras de evento obtenidos correctamente",
                data = extras
            )
        )
    }

    /**
     * Obtiene los extras de tipo VARIABLE_EVENTO con precio para un tipo de evento y fecha
     * @param empresaId ID de la empresa
     * @param tipoEventoId ID del tipo de evento
     * @param fechaEvento Fecha del evento
     */
    @PutMapping("/empresa/{empresaId}/{tipoEventoId}/extras/evento-variable")
    fun getAllExtraEventoVariable(
        @PathVariable empresaId: Long,
        @PathVariable tipoEventoId: Long,
        @RequestBody fechaEvento: LocalDateTime
    ): ResponseEntity<CustomResponse<List<ExtraPrecioDTO>>> {
        val extras = tipoEventoService.getExtrasConPrecio(empresaId, tipoEventoId, fechaEvento, TipoExtra.VARIABLE_EVENTO)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Extras variables de evento obtenidos correctamente",
                data = extras
            )
        )
    }

    /**
     * Obtiene los extras de tipo TIPO_CATERING con precio para un tipo de evento y fecha
     * @param empresaId ID de la empresa
     * @param tipoEventoId ID del tipo de evento
     * @param fechaEvento Fecha del evento
     */
    @PutMapping("/empresa/{empresaId}/{tipoEventoId}/extras/tipo-catering")
    fun getAllTipoCatering(
        @PathVariable empresaId: Long,
        @PathVariable tipoEventoId: Long,
        @RequestBody fechaEvento: LocalDateTime
    ): ResponseEntity<CustomResponse<List<ExtraPrecioDTO>>> {
        val extras = tipoEventoService.getExtrasConPrecio(empresaId, tipoEventoId, fechaEvento, TipoExtra.TIPO_CATERING)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Tipos de catering obtenidos correctamente",
                data = extras
            )
        )
    }

    /**
     * Obtiene los extras de tipo VARIABLE_CATERING con precio para un tipo de evento y fecha
     * @param empresaId ID de la empresa
     * @param tipoEventoId ID del tipo de evento
     * @param fechaEvento Fecha del evento
     */
    @PutMapping("/empresa/{empresaId}/{tipoEventoId}/extras/catering-variable")
    fun getAllCateringExtra(
        @PathVariable empresaId: Long,
        @PathVariable tipoEventoId: Long,
        @RequestBody fechaEvento: LocalDateTime
    ): ResponseEntity<CustomResponse<List<ExtraPrecioDTO>>> {
        val extras = tipoEventoService.getExtrasConPrecio(empresaId, tipoEventoId, fechaEvento, TipoExtra.VARIABLE_CATERING)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Extras variables de catering obtenidos correctamente",
                data = extras
            )
        )
    }

    // ==================== CREAR / ACTUALIZAR TIPOS DE EVENTO ====================

    /**
     * Crea o actualiza un tipo de evento
     * @param tipoEventoDto Datos del tipo de evento a guardar
     */
    @PostMapping
    fun saveTipoEvento(
        @RequestBody tipoEventoDto: TipoEventoDTO
    ): ResponseEntity<CustomResponse<TipoEventoDTO>> {
        val tipoEvento = tipoEventoService.saveTipoEvento(tipoEventoDto)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Tipo de evento guardado correctamente",
                data = tipoEvento
            )
        )
    }

    // ==================== ELIMINAR TIPOS DE EVENTO ====================

    /**
     * Elimina (soft delete) un tipo de evento por su ID
     * @param id ID del tipo de evento
     */
    @DeleteMapping("/{id}")
    fun deleteTipoEvento(
        @PathVariable id: Long
    ): ResponseEntity<CustomResponse<TipoEventoDTO>> {
        val tipoEvento = tipoEventoService.deleteTipoEvento(id)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Tipo de evento eliminado correctamente",
                data = tipoEvento
            )
        )
    }
}