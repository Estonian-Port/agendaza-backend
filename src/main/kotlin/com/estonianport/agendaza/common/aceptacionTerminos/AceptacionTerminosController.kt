package com.estonianport.agendaza.common.aceptacionTerminos

import com.estonianport.agendaza.dto.response.CustomResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/terminos")
class TerminosController(
    private val terminosService: TerminosService
) {

    @GetMapping("/vigente")
    fun vigente() =
        ResponseEntity.ok(
            CustomResponse(
                message = "Términos vigentes",
                data = terminosService.vigente()))
}