package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.ExtraDTO
import com.estonianport.agendaza.dto.PrecioConFechaDTO
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.PrecioConFechaExtra
import com.estonianport.agendaza.model.enums.TipoExtra
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
import java.time.LocalDate

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

    @GetMapping("/getAllExtraEvento")
    fun getAllEvento(): List<ExtraDTO> {
        return extraService.getAllEvento()
    }

    @GetMapping("/getAllExtraCatering")
    fun getAllCatering(): List<ExtraDTO> {
        return extraService.getAllCatering()
    }
    @GetMapping("/getExtra/{id}")
    fun get(@PathVariable("id") id: Long): ExtraDTO {
        val extra = extraService.get(id)!!
        val extraDto = extra.toDTO()

        extraDto.listaTipoEventoId = tipoEventoService.getAllByExtra(extraDto.id).map { it.id }.toMutableSet()
        return extraDto
    }

    @PostMapping("/saveExtra")
    fun save(@RequestBody extraDTO: ExtraDTO): ExtraDTO {

        var extra : Extra = if(extraDTO.id != 0L){
            extraService.get(extraDTO.id)!!
        }else{
            Extra(0, extraDTO.nombre, extraDTO.tipoExtra)
        }

        extra.listaTipoEvento = extraDTO.listaTipoEventoId.map { tipoEventoService.get(it)!! }.toMutableSet()

        extra = extraService.save(extra)

        val empresa = empresaService.get(extraDTO.empresaId)!!

        empresa.listaExtra.add(extra)
        empresaService.save(empresa)

        return extra.toDTO()
    }

    @DeleteMapping("/deleteExtra/{extraId}/{empresaId}")
    fun delete(@PathVariable("extraId") extraId: Long, @PathVariable("empresaId") empresaId: Long): ResponseEntity<ExtraDTO> {
        extraService.deleteExtra(extraId, empresaId)
        return ResponseEntity<ExtraDTO>(HttpStatus.OK)
    }

    @GetMapping("/getAllEventoTipoExtra")
    fun getAllEventoTipoExtra(): MutableSet<TipoExtra> {
        return mutableSetOf(TipoExtra.EVENTO, TipoExtra.VARIABLE_EVENTO)
    }

    @GetMapping("/getAllCateringTipoExtra")
    fun getAllCateringTipoExtra(): MutableSet<TipoExtra> {
        return mutableSetOf(TipoExtra.TIPO_CATERING, TipoExtra.VARIABLE_CATERING)
    }

    @PostMapping("/saveExtraPrecio/{empresaId}/{extraId}")
    fun saveTipoEventoPrecio(@PathVariable("empresaId") empresaId: Long, @PathVariable("extraId") extraId: Long, @RequestBody listaPrecioConFechaDTO : MutableSet<PrecioConFechaDTO>): ResponseEntity<PrecioConFechaDTO> {
        val extra = extraService.get(extraId)!!
        val empresa = empresaService.get(empresaId)!!

        val listaPrecio = empresa.listaPrecioConFechaExtra.filter { it.extra.id == extra.id }

        listaPrecio.forEach{
            if(!listaPrecioConFechaDTO.any { precioConFechaNuevo -> precioConFechaNuevo.id == it.id }){
                val precioViejo = precioConFechaExtraService.get(it.id)!!
                precioViejo.fechaBaja = LocalDate.now()
                precioConFechaExtraService.save(precioViejo)
            }
        }

        listaPrecioConFechaDTO.forEach{

            // Busca el ultimo dia del mes del hasta
            val fechaHasta = it.hasta.plusMonths(1).minusDays(1).plusHours(20).plusMinutes(59).plusSeconds(59)

            precioConFechaExtraService.save(
                PrecioConFechaExtra(
                it.id,
                it.precio,
                it.desde.minusHours(3),
                fechaHasta,
                empresa,
                extra
            )
            )
        }

        return ResponseEntity<PrecioConFechaDTO>(HttpStatus.OK)
    }

    @GetMapping("/getAllExtra/{id}/{pageNumber}")
    fun getAllExtra(@PathVariable("id") id: Long, @PathVariable("pageNumber") pageNumber : Int): List<ExtraDTO> {
        return extraService.extras(id,pageNumber)
    }

    @GetMapping("/getAllExtraFilter/{id}/{pageNumber}/{buscar}")
    fun getAllExtraFilter(@PathVariable("id") id: Long, @PathVariable("pageNumber") pageNumber : Int, @PathVariable("buscar") buscar : String): List<ExtraDTO> {
        return extraService.extrasFiltrados(id, pageNumber, buscar)
        //.filter{ (it.tipoExtra == TipoExtra.EVENTO || it.tipoExtra == TipoExtra.VARIABLE_EVENTO)}
    }

    @GetMapping("/cantExtras/{id}")
    fun cantExtras(@PathVariable("id") id: Long) =  extraService.contadorDeExtras(id)

    @GetMapping("/cantExtrasFiltrados/{id}/{buscar}")
    fun cantExtrasFiltrados(@PathVariable("id") id: Long, @PathVariable("buscar") buscar : String) {
        extraService.contadorDeExtrasFiltrados(id,buscar)
    }

    @GetMapping("/getAllExtraCatering/{id}/{pageNumber}")
    fun getAllExtraCatering(@PathVariable("id") id: Long, @PathVariable("pageNumber") pageNumber : Int): List<ExtraDTO> {
        return extraService.extrasCatering(id,pageNumber)
    }

    @GetMapping("/getAllExtraCateringFilter/{id}/{pageNumber}/{buscar}")
    fun getAllExtraCateringFilter(@PathVariable("id") id: Long, @PathVariable("pageNumber") pageNumber : Int, @PathVariable("buscar") buscar : String): List<ExtraDTO> {
        return extraService.extrasCateringFiltrados(id, pageNumber, buscar)
    }

    @GetMapping("/cantExtraCatering/{id}")
    fun cantExtraCatering(@PathVariable("id") id: Long) : Int {
        return extraService.contadorDeExtrasCatering(id)
    }

    @GetMapping("/cantExtraCateringFilter/{id}/{buscar}")
    fun cantExtraCateringFilter(@PathVariable("id") id: Long, @PathVariable("buscar") buscar : String) : Int {
        return extraService.contadorDeExtrasCateringFilter(id,buscar)
    }

    @GetMapping("/getAllPrecioConFechaByExtraId/{empresaId}/{extraId}")
    fun getAllPrecioConFechaByExtraId(@PathVariable("empresaId") empresaId: Long, @PathVariable("extraId") extraId: Long): List<PrecioConFechaDTO> {
        return empresaService.getAllPrecioConFechaByExtraId(empresaId, extraId)
    }

    @GetMapping("/getAllExtraEventoAgregar/{empresaId}")
    fun getAllExtraEventoAgregar(@PathVariable("empresaId") empresaId: Long): List<ExtraDTO> {
        return extraService.getAllExtraEventoAgregar(empresaId)
    }

    @GetMapping("/getAllExtraCateringAgregar/{empresaId}")
    fun getAllExtraCateringAgregar(@PathVariable("empresaId") empresaId: Long): List<ExtraDTO> {
        return extraService.getAllExtraCaterigAgregar(empresaId)
    }
}