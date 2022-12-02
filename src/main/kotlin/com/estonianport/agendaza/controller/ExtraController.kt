package com.estonianport.agendaza.controller

import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.service.ExtraService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin("*")
class ExtraController {
    @Autowired
    lateinit var extraService: ExtraService

    @GetMapping("/getAllExtra")
    fun abm(): MutableList<Extra>? {
        return extraService.getAll()
    }

    @GetMapping("/getExtra/{id}")
    fun showSave(@PathVariable("id") id: Long): ResponseEntity<Extra>? {
        if (id != 0L) {
            ResponseEntity<Extra>(extraService.get(id), HttpStatus.OK)
        }
        return ResponseEntity<Extra>(HttpStatus.NO_CONTENT)
    }

    @PostMapping("/saveExtra")
    fun save(extra: Extra): ResponseEntity<Extra> {
        return ResponseEntity<Extra>(extraService.save(extra), HttpStatus.OK)
    }

    @GetMapping("/deleteExtra/{id}")
    fun delete(@PathVariable("id") id: Long): ResponseEntity<Extra> {
        /*val extraCatering: ExtraVariableCatering = extraCateringService.get(id)

        // Elimina los subTipoEvento Vinculados
        extraCatering.setListaSubTipoEvento(null)
        extraCateringService.save(extraCatering)

        // Elimina los precios seteados para este extra variable
        for (precioConFecha in extraCatering.getListaPrecioConFecha()) {
            precioConFechaExtraVariableCateringService.delete(precioConFecha.getId())
        }*/
        extraService.delete(id)
        return ResponseEntity<Extra>(HttpStatus.OK)
    }
}