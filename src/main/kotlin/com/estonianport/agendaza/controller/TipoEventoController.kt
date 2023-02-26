package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.PrecioConFechaDto
import com.estonianport.agendaza.dto.TipoEventoDto
import com.estonianport.agendaza.model.Capacidad
import com.estonianport.agendaza.model.Duracion
import com.estonianport.agendaza.model.PrecioConFechaTipoEvento
import com.estonianport.agendaza.model.TipoEvento
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
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.LocalTime


@RestController
@CrossOrigin("*")
class TipoEventoController {

    @Autowired
    lateinit var  tipoEventoService: TipoEventoService

    @Autowired
    lateinit var  capacidadService: CapacidadService

    @Autowired
    lateinit var  servicioService: ServicioService

    @Autowired
    lateinit var  precioConFechaTipoEventoService: PrecioConFechaTipoEventoService

    @Autowired
    lateinit var  extraService: ExtraService

    @Autowired
    lateinit var  empresaService: EmpresaService

    @GetMapping("/getAllTipoEvento")
    fun getAll(): MutableList<TipoEvento>? {
        return tipoEventoService.getAll()
    }

    @GetMapping("/getTipoEvento/{id}")
    fun get(@PathVariable("id") id: Long): ResponseEntity<TipoEvento>? {
        if (id != 0L) {
            return ResponseEntity<TipoEvento>(tipoEventoService.get(id), HttpStatus.OK)
        }
        return ResponseEntity<TipoEvento>(HttpStatus.NO_CONTENT)
    }

    @PostMapping("/saveTipoEvento")
    fun save(@RequestBody tipoEventoDto: TipoEventoDto): TipoEvento {
        val listaCapacidad: MutableList<Capacidad>? = capacidadService.getAll()

        // Reutilizar capacidades ya guardadas
        if (listaCapacidad != null && listaCapacidad.size != 0) {
            for (capacidad in listaCapacidad) {
                if (capacidad.capacidadAdultos == tipoEventoDto.capacidad.capacidadAdultos
                    && capacidad.capacidadNinos == tipoEventoDto.capacidad.capacidadNinos) {
                    tipoEventoDto.capacidad = capacidad
                }
            }
        }

        return tipoEventoService.save(TipoEvento(tipoEventoDto.id, tipoEventoDto.nombre,
            tipoEventoDto.duracion, tipoEventoDto.capacidad,
            LocalTime.of(tipoEventoDto.cantidadDuracion.hour, tipoEventoDto.cantidadDuracion.minute),
            empresaService.get(tipoEventoDto.empresaId)!!))
    }

    @DeleteMapping("/deleteTipoEvento/{id}")
    fun delete(@PathVariable("id") id: Long): ResponseEntity<TipoEvento> {

        val tipoEventoEliminar : TipoEvento =  tipoEventoService.get(id)!!

        // Elimina en los servicios
        tipoEventoEliminar.listaServicio.forEach { servicio ->
            if(servicio.listaTipoEvento.contains(tipoEventoEliminar)){
                servicio.listaTipoEvento.remove(tipoEventoEliminar)
                servicioService.save(servicio)
            }
        }
        // Elimina en los extra
        tipoEventoEliminar.listaExtra.forEach { extra ->
            if(extra.listaTipoEvento.contains(tipoEventoEliminar)){
                extra.listaTipoEvento.remove(tipoEventoEliminar)
                extraService.save(extra)
            }
        }

        // Elimina en los precio con fecha
        tipoEventoEliminar.listaPrecioConFecha.forEach { precioConFecha ->
            if(precioConFecha.tipoEvento == tipoEventoEliminar){
                precioConFechaTipoEventoService.delete(precioConFecha.id)
            }
        }

        // Eliminar tipoEventoEliminar
        tipoEventoService.delete(id)

        return ResponseEntity<TipoEvento>(HttpStatus.OK)
    }

    @GetMapping("/getAllDuracion")
    fun getAllDuracion(@RequestBody listaPrecioConFechaDto: TipoEventoDto): ResponseEntity<MutableSet<Duracion>>? {
        return ResponseEntity<MutableSet<Duracion>>(Duracion.values().toMutableSet(), HttpStatus.OK)
    }

    @GetMapping("/getAllPrecioConFechaByTipoEventoId/{id}")
    fun getAllDuracion(@PathVariable("id") id: Long): ResponseEntity<MutableSet<PrecioConFechaDto>>? {
        val tipoEvento = tipoEventoService.get(id)!!

        // Filtra years anteriores al corriente para que ya no figuren a la hora de cargarlos
        val listaPrecioSinYearAnterior = tipoEvento.listaPrecioConFecha.filter { it.desde.year >= LocalDateTime.now().year }

        val listaPrecioConFechaDto : MutableSet<PrecioConFechaDto> = mutableSetOf()


        listaPrecioSinYearAnterior.forEach{
            listaPrecioConFechaDto.add(PrecioConFechaDto(
                it.id,
                it.desde,
                it.hasta,
                it.precio,
                it.empresa.id,
                it.tipoEvento.id
            ))
        }

        return ResponseEntity<MutableSet<PrecioConFechaDto>>(listaPrecioConFechaDto, HttpStatus.OK)
    }

    @PostMapping("/saveTipoEventoPrecio")
    fun saveTipoEventoPrecio(@RequestBody listaPrecioConFechaDto : MutableSet<PrecioConFechaDto>): ResponseEntity<String>? {
        val tipoEvento = tipoEventoService.get(listaPrecioConFechaDto.first().itemId)!!
        val empresa = empresaService.get(listaPrecioConFechaDto.first().empresaId)!!

        tipoEvento.listaPrecioConFecha.forEach{
            if(!listaPrecioConFechaDto.any { it2 -> it2.id == it.id  }){
                precioConFechaTipoEventoService.delete(it.id)
            }
        }

        listaPrecioConFechaDto.forEach{

            precioConFechaTipoEventoService.save(PrecioConFechaTipoEvento(
                it.id,
                it.precio,
                it.desde,
                it.hasta,
                empresa,
                tipoEvento
            ))
        }

        return ResponseEntity<String>(HttpStatus.OK)
    }


}