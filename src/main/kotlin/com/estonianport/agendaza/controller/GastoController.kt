package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.GastoDTO
import com.estonianport.agendaza.dto.TotalesPagosMes
import com.estonianport.agendaza.dto.response.CustomResponse
import com.estonianport.agendaza.model.enums.TipoGasto
import com.estonianport.agendaza.service.GastoService
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
@RequestMapping("/v1/gastos")
@CrossOrigin("*")
class GastoController(
    private val gastoService: GastoService
) {

    @GetMapping("/tipos")
    fun getAllTiposGasto(): ResponseEntity<CustomResponse<Array<TipoGasto>>> {
        val tipos = TipoGasto.entries.toTypedArray()
        return ResponseEntity.ok(
            CustomResponse(
                message = "Tipos de gasto obtenidos correctamente",
                data = tipos
            )
        )
    }

    @GetMapping("/{id}")
    fun getGasto(@PathVariable id: Long): ResponseEntity<CustomResponse<GastoDTO>> {
        val gasto = gastoService.getGastoDTO(id)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Gasto obtenido correctamente",
                data = gasto
            )
        )
    }

    @GetMapping("/empresa/{empresaId}/mes")
    fun getAllGastoByMes(
        @PathVariable empresaId: Long,
        @RequestParam mes: Int,
        @RequestParam anio: Int
    ): ResponseEntity<CustomResponse<List<GastoDTO>>> {
        val gastos = gastoService.getAllGastoByMes(empresaId, mes, anio)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Gastos del mes obtenidos correctamente",
                data = gastos
            )
        )
    }

    @GetMapping("/empresa/{empresaId}/resumen")
    fun getResumenGastosMes(
        @PathVariable empresaId: Long,
        @RequestParam mes: Int,
        @RequestParam anio: Int
    ): ResponseEntity<CustomResponse<TotalesPagosMes>> {
        val resumen = gastoService.totalesByRango(empresaId, mes, anio)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Resumen de gastos del mes obtenido correctamente",
                data = resumen
            )
        )
    }

    @PostMapping
    fun saveGasto(@RequestBody gastoDTO: GastoDTO): ResponseEntity<CustomResponse<GastoDTO>> {
        val gasto = gastoService.saveGasto(gastoDTO)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Gasto guardado correctamente",
                data = gasto
            )
        )
    }

    @DeleteMapping("/{id}")
    fun deleteGasto(@PathVariable id: Long): ResponseEntity<CustomResponse<String>> {
        gastoService.delete(id)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Gasto eliminado correctamente",
                data = "OK"
            )
        )
    }
}
