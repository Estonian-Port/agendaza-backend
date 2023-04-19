package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.ExtraDTO
import com.estonianport.agendaza.dto.PrecioConFechaDto
import com.estonianport.agendaza.dto.ServicioDTO
import com.estonianport.agendaza.dto.TimeDTO
import com.estonianport.agendaza.dto.TipoEventoDTO
import com.estonianport.agendaza.model.Capacidad
import com.estonianport.agendaza.model.Duracion
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.PrecioConFechaTipoEvento
import com.estonianport.agendaza.model.Servicio
import com.estonianport.agendaza.model.TipoEvento
import com.estonianport.agendaza.model.TipoExtra
import com.estonianport.agendaza.service.CapacidadService
import com.estonianport.agendaza.service.EmpresaService
import com.estonianport.agendaza.service.ExtraService
import com.estonianport.agendaza.service.PrecioConFechaTipoEventoService
import com.estonianport.agendaza.service.ServicioService
import com.estonianport.agendaza.service.TipoEventoService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


@RestController
@CrossOrigin("*")
class TipoEventoController {

    @Autowired
    lateinit var tipoEventoService: TipoEventoService

    @Autowired
    lateinit var capacidadService: CapacidadService

    @Autowired
    lateinit var servicioService: ServicioService

    @Autowired
    lateinit var precioConFechaTipoEventoService: PrecioConFechaTipoEventoService

    @Autowired
    lateinit var extraService: ExtraService

    @Autowired
    lateinit var empresaService: EmpresaService

    @GetMapping("/getAllTipoEvento")
    fun getAll(): MutableList<TipoEvento>? {
        return tipoEventoService.getAll()
    }

    @GetMapping("/getTipoEvento/{id}")
    fun get(@PathVariable("id") id: Long): TipoEventoDTO {
        return tipoEventoService.get(id)!!.toDTO()
    }

    @PostMapping("/saveTipoEvento")
    fun save(@RequestBody tipoEventoDto: TipoEventoDTO): TipoEventoDTO {
        tipoEventoDto.capacidad = capacidadService.reutilizarCapacidad(tipoEventoDto.capacidad)

        return tipoEventoService.save(
            TipoEvento(
                tipoEventoDto.id, tipoEventoDto.nombre,
                tipoEventoDto.duracion, tipoEventoDto.capacidad,
                LocalTime.of(tipoEventoDto.cantidadDuracion.hour, tipoEventoDto.cantidadDuracion.minute),
                empresaService.get(tipoEventoDto.empresaId)!!
            )
        ).toDTO()
    }

    @DeleteMapping("/deleteTipoEvento/{id}")
    fun delete(@PathVariable("id") id: Long): TipoEventoDTO {
        val tipoEventoEliminar = tipoEventoService.get(id)!!
        tipoEventoEliminar.fechaBaja = LocalDate.now()
        tipoEventoService.save(tipoEventoEliminar)

        // Deja los precios con fecha del tipo evento eliminado sin fecha baja
        /*val listaPrecioTipoEventoEliminar = tipoEventoEliminar.empresa.listaPrecioConFechaTipoEvento.filter {
            it.id == tipoEventoEliminar.id
        }

        listaPrecioTipoEventoEliminar.forEach {
            it.fechaBaja = LocalDate.now()
            precioConFechaTipoEventoService.save(it)
        }*/

        return tipoEventoEliminar.toDTO()
    }

    @GetMapping("/getAllDuracion")
    fun getAllDuracion(): MutableSet<Duracion> {
        return Duracion.values().toMutableSet()
    }

    @GetMapping("/getListaTipoEventoOfExtra/{id}")
    fun getListaTipoEventoByExtra(@PathVariable("id") id: Long): List<TipoEventoDTO> {
        return tipoEventoService.listaTipoEventoToListaTipoEventoDTO(
            tipoEventoService.getAllByExtra(extraService.get(id)!!)!!
        )
    }

    @GetMapping("/getListaTipoEventoByServicio/{id}")
    fun getListaTipoEventoByServicio(@PathVariable("id") id: Long): List<TipoEventoDTO> {
        return tipoEventoService.listaTipoEventoToListaTipoEventoDTO(
            tipoEventoService.getAllByServicio(servicioService.get(id)!!)!!
        )
    }

    @GetMapping("/getAllPrecioConFechaByTipoEventoId/{id}")
    fun getAllPrecioConFechaByTipoEventoId(@PathVariable("id") id: Long): List<PrecioConFechaDto> {
        val tipoEvento = tipoEventoService.get(id)!!

        // Filtra years anteriores al corriente para que ya no figuren a la hora de cargarlos
        val listaPrecioSinYearAnterior = tipoEvento.empresa.listaPrecioConFechaTipoEvento.filter {
            it.tipoEvento.id == tipoEvento.id &&
            it.tipoEvento.fechaBaja == null &&
            it.desde.year >= LocalDateTime.now().year &&
            it.fechaBaja == null
        }

        return listaPrecioSinYearAnterior.map { it.toDTO() }

    }

    @PostMapping("/saveTipoEventoPrecio/{id}")
    fun saveTipoEventoPrecio(@PathVariable("id") id: Long, @RequestBody listaPrecioConFechaDto : MutableSet<PrecioConFechaDto>): ResponseEntity<PrecioConFechaDto> {
        val tipoEvento = tipoEventoService.get(id)!!
        val empresa = empresaService.get(tipoEvento.empresa.id)!!

        val listaPrecio = empresa.listaPrecioConFechaTipoEvento.filter { it.tipoEvento.id == tipoEvento.id }

        listaPrecio.forEach{
            if(!listaPrecioConFechaDto.any { precioConFechaNuevo -> precioConFechaNuevo.id == it.id }){
                val precioViejo = precioConFechaTipoEventoService.get(it.id)!!
                precioViejo.fechaBaja = LocalDate.now()
                precioConFechaTipoEventoService.save(precioViejo)
            }
        }

        listaPrecioConFechaDto.forEach{

            // Busca el ultimo dia del mes del hasta
            val fechaHasta = it.hasta.plusMonths(1).minusDays(1).plusHours(20).plusMinutes(59).plusSeconds(59)

            precioConFechaTipoEventoService.save(PrecioConFechaTipoEvento(
                it.id,
                it.precio,
                it.desde.minusHours(3),
                fechaHasta,
                empresa,
                tipoEvento
            ))
        }
        return ResponseEntity<PrecioConFechaDto>(HttpStatus.OK)
    }

    @GetMapping("/getAllServicioByTipoEventoId/{id}")
    fun getAllServicioByTipoEventoId(@PathVariable("id") id: Long): List<ServicioDTO>{
        return servicioService.fromListaServicioToListaServicioDto(
            tipoEventoService.get(id)!!.listaServicio.filter { it.fechaBaja == null }
        )
    }

    @PutMapping("/getAllExtraEventoByTipoEventoIdAndFecha/{id}")
    fun getAllExtraEventoByTipoEventoIdAndFecha(@PathVariable("id") id: Long, @RequestBody fechaEvento : LocalDateTime): List<ExtraDTO> {
        return extraService.fromListaExtraToListaExtraDtoByFilter(
            tipoEventoService.get(id)!!.listaExtra.filter { it.fechaBaja == null }.toMutableSet(), fechaEvento, TipoExtra.EVENTO)
    }

    @PutMapping("/getAllExtraEventoVariableByTipoEventoIdAndFecha/{id}")
    fun getAllExtraEventoVariableByTipoEventoIdAndFecha(@PathVariable("id") id: Long, @RequestBody fechaEvento : LocalDateTime): List<ExtraDTO> {
        return extraService.fromListaExtraToListaExtraDtoByFilter(
            tipoEventoService.get(id)!!.listaExtra.filter { it.fechaBaja == null }.toMutableSet(), fechaEvento, TipoExtra.VARIABLE_EVENTO)
    }

    @PutMapping("/getAllTipoCateringByTipoEventoIdAndFecha/{id}")
    fun getAllTipoCateringByTipoEventoIdAndFecha(@PathVariable("id") id: Long, @RequestBody fechaEvento : LocalDateTime): List<ExtraDTO> {
        return extraService.fromListaExtraToListaExtraDtoByFilter(
            tipoEventoService.get(id)!!.listaExtra.filter { it.fechaBaja == null }.toMutableSet(), fechaEvento, TipoExtra.TIPO_CATERING)
    }

    @PutMapping("/getAllCateringExtraByTipoEventoIdAndFecha/{id}")
    fun getAllCateringExtraByTipoEventoId(@PathVariable("id") id: Long, @RequestBody fechaEvento : LocalDateTime): List<ExtraDTO> {
        return extraService.fromListaExtraToListaExtraDtoByFilter(
            tipoEventoService.get(id)!!.listaExtra.filter { it.fechaBaja == null }.toMutableSet(), fechaEvento, TipoExtra.VARIABLE_CATERING)
    }

    @GetMapping("/getDuracionByTipoEventoId/{id}")
    fun getDuracionByTipoEventoId(@PathVariable("id") id: Long): LocalTime {
        return tipoEventoService.get(id)!!.cantidadDuracion
    }

    @GetMapping("/getCapacidadByTipoEventoId/{id}")
    fun getCapacidadByTipoEventoId(@PathVariable("id") id: Long): Capacidad {
        return tipoEventoService.get(id)!!.capacidad
    }

    @GetMapping("/findExtraNinoByTipoEventoId/{id}")
    fun findExtraNinoByTipoEventoId(@PathVariable("id") id: Long): Extra? {
        return tipoEventoService.get(id)!!.listaExtra.find { it.nombre == "Niño" }
    }

    @GetMapping("/findExtraCamareraByTipoEventoId/{id}")
    fun findExtraCamareraByTipoEventoId(@PathVariable("id") id: Long): Extra? {
        return tipoEventoService.get(id)!!.listaExtra.find { it.nombre.split(" ")[0] == "Camarera" }
    }

    @PutMapping("/getTimeEndByTipoEventoIdAndTimeStart/{id}")
    fun getTimeEndByTipoEventoIdAndTimeStart(@PathVariable("id") id: Long, @RequestBody timeStart: TimeDTO): LocalTime? {
        return tipoEventoService.get(id)!!.cantidadDuracion.plusHours(
            timeStart.hour.toLong()).plusMinutes(timeStart.minute.toLong())
    }

    @PutMapping("/getPrecioByTipoEventoIdAndFecha/{id}")
    fun getPrecioByTipoEventoIdAndFecha(@PathVariable("id") id: Long, @RequestBody fechaEvento: LocalDateTime): Double? {
        val tipoEvento = tipoEventoService.get(id)!!
        return tipoEvento.empresa.getPrecioOfTipoEvento(tipoEvento, fechaEvento)
    }
}