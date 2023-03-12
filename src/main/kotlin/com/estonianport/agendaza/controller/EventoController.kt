package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.EventoAgendaDto
import com.estonianport.agendaza.dto.EventoPagoDto
import com.estonianport.agendaza.dto.EventoReservaDto
import com.estonianport.agendaza.dto.PagoDto
import com.estonianport.agendaza.model.Agregados
import com.estonianport.agendaza.model.CateringEvento
import com.estonianport.agendaza.model.CateringEventoExtraVariableCatering
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Estado
import com.estonianport.agendaza.model.Evento
import com.estonianport.agendaza.model.EventoExtraVariableTipoEvento
import com.estonianport.agendaza.model.TipoEvento
import com.estonianport.agendaza.model.Usuario
import com.estonianport.agendaza.service.CapacidadService
import com.estonianport.agendaza.service.EmpresaService
import com.estonianport.agendaza.service.EventoService
import com.estonianport.agendaza.service.ExtraService
import com.estonianport.agendaza.service.TipoEventoService
import com.estonianport.agendaza.service.UsuarioService
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
class EventoController {

    @Autowired
    lateinit var eventoService : EventoService

    @Autowired
    lateinit var empresaService : EmpresaService

    @Autowired
    lateinit var tipoEventoService : TipoEventoService

    @Autowired
    lateinit var capacidadService : CapacidadService

    @Autowired
    lateinit var extraService : ExtraService

    @Autowired
    lateinit var usuarioService : UsuarioService

    @GetMapping("/getAllEvento")
    fun getAll(): MutableList<Evento>? {
        return eventoService.getAll()
    }

    @GetMapping("/getEvento/{id}")
    fun get(@PathVariable("id") id: Long): ResponseEntity<Evento>? {
        if (id != 0L) {
            return ResponseEntity<Evento>(eventoService.get(id), HttpStatus.OK)
        }
        return ResponseEntity<Evento>(HttpStatus.NO_CONTENT)
    }

    @PostMapping("/saveEvento")
    fun save(@RequestBody eventoReservaDto: EventoReservaDto): ResponseEntity<Long> {

        val empresa : Empresa = empresaService.get(eventoReservaDto.empresaId)!!
        val tipoEvento : TipoEvento = tipoEventoService.get(eventoReservaDto.tipoEventoId)!!
        val encargado : Usuario = usuarioService.get(eventoReservaDto.encargadoId)!!

        // Generar codigo de reserva
        if(eventoReservaDto.codigo.isEmpty()){
            eventoReservaDto.codigo = eventoService.generateCodigoForEventoOfEmpresa(empresa)
        }

        // Capacidad evento
        eventoReservaDto.capacidad = capacidadService.reutilizarCapacidad(eventoReservaDto.capacidad)


        // Creacion de Agregados

        val agregados : Agregados = Agregados(
            eventoReservaDto.agregados.id,
            eventoReservaDto.agregados.extraOtro,
            eventoReservaDto.agregados.descuento,
            mutableSetOf(),
            mutableSetOf())

        eventoReservaDto.agregados.listaExtra.forEach {
            agregados.listaExtra.add(extraService.get(it)!!)
        }

        eventoReservaDto.agregados.listaExtraVariable.forEach {
            val extra = extraService.get(it.id)!!
            val extraVariable = EventoExtraVariableTipoEvento(0, extra ,it.cantidad)
            agregados.listaEventoExtraVariable.add(extraVariable)
        }

        agregados.listaEventoExtraVariable.forEach{
            it.agregados = agregados
        }

        // Creacion de Catering

        val catering : CateringEvento = CateringEvento(
            eventoReservaDto.catering.id,
            eventoReservaDto.catering.presupuesto,
            eventoReservaDto.catering.cateringOtro,
            eventoReservaDto.catering.descripcion,
            mutableSetOf(),
            mutableSetOf())

        eventoReservaDto.catering.listaExtraTipoCatering.forEach{
            catering.listaTipoCatering.add(extraService.get(it)!!)
        }

        eventoReservaDto.catering.listaExtraCateringVariable.forEach{
            val extraVariable = CateringEventoExtraVariableCatering(0, extraService.get(it.id)!!, it.cantidad)
            catering.listaCateringExtraVariableCatering.add(extraVariable)
        }

        catering.listaCateringExtraVariableCatering.forEach{
            it.cateringEvento = catering
        }

        // Inicializacion Evento

        val evento : Evento = Evento(
            eventoReservaDto.id,
            eventoReservaDto.nombre,
            tipoEvento,
            eventoReservaDto.inicio,
            eventoReservaDto.fin,
            eventoReservaDto.capacidad,
            agregados,
            catering,
            eventoReservaDto.presupuesto,
            encargado,
            eventoReservaDto.cliente,
            eventoReservaDto.codigo,
            eventoReservaDto.estado
        )

        // vincula el evento a la empresa
        evento.listaEmpresa.add(empresa)

        return ResponseEntity<Long>(eventoService.save(evento).id, HttpStatus.OK)
    }

    @DeleteMapping("/deleteEvento/{id}")
    fun delete(@PathVariable("id") id: Long): ResponseEntity<Evento> {
        eventoService.delete(id)
        return ResponseEntity<Evento>(HttpStatus.OK)
    }

    @GetMapping("/getAllEstado")
    fun getAllEstado(): ResponseEntity<MutableSet<Estado>>? {
        return ResponseEntity<MutableSet<Estado>>(Estado.values().toMutableSet(), HttpStatus.OK)
    }

    @GetMapping("/getEventoPago/{id}")
    fun getEventoPago(@PathVariable("id") id: Long): ResponseEntity<EventoPagoDto>? {
        val evento = eventoService.get(id)!!
        val total = evento.presupuesto + evento.catering.presupuesto
        val listaPago: MutableSet<PagoDto> = mutableSetOf()
        evento.listaPago.forEach {
            listaPago.add(PagoDto(it.id, it.monto, it.evento.codigo, it.medioDePago, it.evento.nombre, it.fecha))
        }

        return ResponseEntity<EventoPagoDto>(EventoPagoDto(evento.id, evento.nombre, evento.codigo, total, listaPago), HttpStatus.OK)
    }

}