package com.estonianport.agendaza.controller

import com.estonianport.agendaza.service.BalanceService
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/balance")
@CrossOrigin("*")
class BalanceController(
    private val balanceService: BalanceService
) {

    /**
     * Descarga el balance en PDF (apaisado) de una empresa para un rango de meses.
     * Ej: /v1/balance/empresa/1/pdf?desdeMes=10&desdeAnio=2023&hastaMes=9&hastaAnio=2024
     */
    @GetMapping("/empresa/{empresaId}/pdf")
    fun descargarBalance(
        @PathVariable empresaId: Long,
        @RequestParam desdeMes: Int,
        @RequestParam desdeAnio: Int,
        @RequestParam hastaMes: Int,
        @RequestParam hastaAnio: Int
    ): ResponseEntity<ByteArray> {
        val pdf = balanceService.generarBalancePdf(empresaId, desdeMes, desdeAnio, hastaMes, hastaAnio)
        val nombre = "balance_%04d-%02d_a_%04d-%02d.pdf".format(desdeAnio, desdeMes, hastaAnio, hastaMes)

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$nombre\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf)
    }
}