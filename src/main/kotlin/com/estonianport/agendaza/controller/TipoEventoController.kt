package com.estonianport.agendaza.controller

import com.estonianport.agendaza.model.Capacidad
import com.estonianport.agendaza.model.TipoEvento
import com.estonianport.agendaza.service.CapacidadService
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


@RestController
@CrossOrigin("*")
class TipoEventoController {

    @Autowired
    lateinit var  tipoEventoService: TipoEventoService

    @Autowired
    lateinit var  capacidadService: CapacidadService

    @Autowired
    lateinit var  servicioService: ServicioService

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
    fun save(@RequestBody tipoEvento: TipoEvento): TipoEvento {
        val listaCapacidad: MutableList<Capacidad>? = capacidadService.getAll()

        // Reutilizar capacidades ya guardadas
        if (listaCapacidad != null && listaCapacidad.size != 0) {
            for (capacidad in listaCapacidad) {
                if (capacidad.capacidadAdultos == tipoEvento.capacidad.capacidadAdultos
                    && capacidad.capacidadNinos == tipoEvento.capacidad.capacidadNinos) {
                    tipoEvento.capacidad = capacidad
                }
            }
        }

        return tipoEventoService.save(tipoEvento)
    }

    @DeleteMapping("/deleteTipoEvento/{id}")
    fun delete(@PathVariable("id") id: Long): ResponseEntity<TipoEvento> {

        var tipoEventoEliminar : TipoEvento? =  tipoEventoService.get(id)

        if (tipoEventoEliminar != null) {
            var listaServicios = tipoEventoEliminar.listaServicio

            // Elimina en los servicio el tipoEventoEliminar
            listaServicios.forEach { servicio ->
                if(servicio.listaTipoEvento.contains(tipoEventoEliminar)){
                    servicio.listaTipoEvento.remove(tipoEventoEliminar)
                    servicioService.save(servicio)
                }
            }

            // Eliminar en tipoEventoEliminar los servicios
            tipoEventoEliminar.listaServicio.clear()
            tipoEventoEliminar.listaExtra.clear()
            tipoEventoEliminar.listaPrecioConFecha.clear()

            // Setea una capacidad sin guardar en la DB a tipoEventoEliminar
            tipoEventoEliminar.capacidad = Capacidad(0,0,0)

            // Eliminar tipoEventoEliminar
            tipoEventoService.delete(id)
        }
        return ResponseEntity<TipoEvento>(HttpStatus.OK)
    }
}