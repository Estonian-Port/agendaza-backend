package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.GenericItemDTO
import com.estonianport.agendaza.dto.response.CustomResponse
import com.estonianport.agendaza.service.ClausulaService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/clausula")
@CrossOrigin("*")
class ClausulaController(
    private val clausulaService: ClausulaService
){

    @PostMapping("/save")
    fun save(@RequestBody clausulaDTO: GenericItemDTO): GenericItemDTO {
        return clausulaService.saveClausula(clausulaDTO)
    }

    @DeleteMapping("/delete/{clausulaId}/{empresaId}")
    fun delete(@PathVariable("clausulaId") clausulaId: Long, @PathVariable("empresaId") empresaId: Long): ResponseEntity<Void> {
        clausulaService.delete(clausulaId, empresaId)
        return ResponseEntity.ok().build()
    }

    /**
     * Obtiene las cláusulas de una empresa con paginación y filtro opcional por nombre.
     */
    @GetMapping("/empresa/{empresaId}")
    fun getPage(
        @PathVariable empresaId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(required = false) buscar: String?
    ): ResponseEntity<CustomResponse<List<GenericItemDTO>>> {
        val clausulas = clausulaService.getPage(empresaId, page, buscar)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Cláusulas obtenidas correctamente",
                data = clausulas
            )
        )
    }

    /**
     * Obtiene la cantidad total de cláusulas activas de una empresa, opcionalmente filtradas por nombre.
     */
    @GetMapping("/empresa/{empresaId}/count")
    fun count(
        @PathVariable empresaId: Long,
        @RequestParam(required = false) buscar: String?
    ): ResponseEntity<CustomResponse<Int>> {
        val cantidad = clausulaService.count(empresaId, buscar)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Cantidad de cláusulas obtenida correctamente",
                data = cantidad
            )
        )
    }

}
