package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.ExtraDto
import com.estonianport.agendaza.dto.PrecioConFechaDto
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.PrecioConFechaExtra
import com.estonianport.agendaza.model.PrecioConFechaTipoEvento
import com.estonianport.agendaza.model.Servicio
import com.estonianport.agendaza.model.TipoEvento
import com.estonianport.agendaza.model.TipoExtra
import com.estonianport.agendaza.service.EmpresaService
import com.estonianport.agendaza.service.ExtraService
import com.estonianport.agendaza.service.PrecioConFechaExtraService
import com.estonianport.agendaza.service.TipoEventoService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@CrossOrigin("*")
class ExtraController {

    @Autowired
    lateinit var extraService: ExtraService

    @Autowired
    lateinit var tipoEventoService : TipoEventoService

    @Autowired
    lateinit var empresaService: EmpresaService

    @Autowired
    lateinit var precioConFechaExtraService : PrecioConFechaExtraService

    @GetMapping("/getAllExtra")
    fun abm(): MutableList<Extra>? {
        return extraService.getAll()
    }

    @GetMapping("/getExtra/{id}")
    fun showSave(@PathVariable("id") id: Long): ResponseEntity<ExtraDto>? {
        if (id != 0L) {
            val extra = extraService.get(id)!!
            val extraDto = ExtraDto(extra.id, extra.nombre, extra.tipoExtra, extra.empresa.id)
            extraDto.listaTipoEventoId = extra.listaTipoEvento.map { it.id }.toMutableSet()
            return ResponseEntity<ExtraDto>(extraDto, HttpStatus.OK)
        }
        return ResponseEntity<ExtraDto>(HttpStatus.NO_CONTENT)
    }

    @GetMapping("/getExtraListaTipoEvento/{id}")
    fun getListaTipoEvento(@PathVariable("id") id: Long): ResponseEntity<MutableSet<TipoEvento>>? {
        if (id != 0L) {
            return ResponseEntity<MutableSet<TipoEvento>>(extraService.get(id)?.listaTipoEvento, HttpStatus.OK)
        }
        return ResponseEntity<MutableSet<TipoEvento>>(HttpStatus.NO_CONTENT)
    }

    @PostMapping("/saveExtra")
    fun save(@RequestBody extraDto: ExtraDto): ResponseEntity<Extra> {
        val listaTipoEvento : MutableSet<TipoEvento> = mutableSetOf()

        extraDto.listaTipoEventoId.forEach {
            tipoEventoService.get(it)?.let { it1 -> listaTipoEvento.add(it1) }
        }

        val extra = Extra(extraDto.id, extraDto.nombre, extraDto.tipoExtra, empresaService.get(extraDto.empresaId)!!)
        extra.listaTipoEvento = listaTipoEvento

        return ResponseEntity<Extra>(extraService.save(extra), HttpStatus.OK)
    }

    @DeleteMapping("/deleteExtra/{id}")
    fun delete(@PathVariable("id") id: Long): ResponseEntity<Extra> {
        extraService.delete(id)
        return ResponseEntity<Extra>(HttpStatus.OK)
    }

    @GetMapping("/getAllEventoTipoExtra")
    fun getAllEventoTipoExtra(): ResponseEntity<MutableSet<TipoExtra>>? {
        return ResponseEntity<MutableSet<TipoExtra>>(mutableSetOf(TipoExtra.EVENTO, TipoExtra.VARIABLE_EVENTO),HttpStatus.OK)
    }

    @GetMapping("/getAllCateringTipoExtra")
    fun getAllCateringTipoExtra(): ResponseEntity<MutableSet<TipoExtra>>? {
        return ResponseEntity<MutableSet<TipoExtra>>(mutableSetOf(TipoExtra.TIPO_CATERING, TipoExtra.VARIABLE_CATERING),HttpStatus.OK)
    }

    @GetMapping("/getAllPrecioConFechaByExtraId/{id}")
    fun getAllDuracion(@PathVariable("id") id: Long): ResponseEntity<MutableSet<PrecioConFechaDto>>? {
        val extra = extraService.get(id)!!

        // Filtra years anteriores al corriente para que ya no figuren a la hora de cargarlos
        val listaPrecioSinYearAnterior = extra.listaPrecioConFecha.filter { it.desde.year >= LocalDateTime.now().year }

        val listaPrecioConFechaDto : MutableSet<PrecioConFechaDto> = mutableSetOf()


        listaPrecioSinYearAnterior.forEach{
            listaPrecioConFechaDto.add(
                PrecioConFechaDto(
                it.id,
                it.desde,
                it.hasta,
                it.precio,
                it.empresa.id,
                it.extra.id
            )
            )
        }

        return ResponseEntity<MutableSet<PrecioConFechaDto>>(listaPrecioConFechaDto, HttpStatus.OK)
    }

    @PostMapping("/saveExtraPrecio")
    fun saveTipoEventoPrecio(@RequestBody listaPrecioConFechaDto : MutableSet<PrecioConFechaDto>): ResponseEntity<String>? {
        val extra = extraService.get(listaPrecioConFechaDto.first().itemId)!!
        val empresa = empresaService.get(listaPrecioConFechaDto.first().empresaId)!!

        extra.listaPrecioConFecha.forEach{
            if(!listaPrecioConFechaDto.any { it2 -> it2.id == it.id  }){
                precioConFechaExtraService.delete(it.id)
            }
        }

        listaPrecioConFechaDto.forEach{

            precioConFechaExtraService.save(
                PrecioConFechaExtra(
                it.id,
                it.precio,
                it.desde,
                it.hasta,
                empresa,
                extra
            )
            )
        }

        return ResponseEntity<String>(HttpStatus.OK)
    }

}