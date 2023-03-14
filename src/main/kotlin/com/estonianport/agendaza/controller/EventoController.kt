package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.AgregadosDto
import com.estonianport.agendaza.dto.CateringEventoDto
import com.estonianport.agendaza.dto.EventoBuscarFechaDto
import com.estonianport.agendaza.dto.EventoCateringDto
import com.estonianport.agendaza.dto.EventoExtraDto
import com.estonianport.agendaza.dto.EventoHoraDto
import com.estonianport.agendaza.dto.EventoPagoDto
import com.estonianport.agendaza.dto.EventoReservaDto
import com.estonianport.agendaza.dto.EventoVerDto
import com.estonianport.agendaza.dto.PagoDto
import com.estonianport.agendaza.model.Agregados
import com.estonianport.agendaza.model.CateringEvento
import com.estonianport.agendaza.model.CateringEventoExtraVariableCatering
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Estado
import com.estonianport.agendaza.model.Evento
import com.estonianport.agendaza.model.EventoExtraVariableTipoEvento
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.TipoEvento
import com.estonianport.agendaza.model.Usuario
import com.estonianport.agendaza.service.CapacidadService
import com.estonianport.agendaza.service.EmpresaService
import com.estonianport.agendaza.service.EventoExtraVariableTipoEventoService
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
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.*

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

    @Autowired
    lateinit var eventoExtraVariableTipoEventoService : EventoExtraVariableTipoEventoService

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
        val agregados = Agregados(
            eventoReservaDto.agregados.id,
            eventoReservaDto.agregados.extraOtro,
            eventoReservaDto.agregados.descuento,
            mutableSetOf(),
            mutableSetOf())

        eventoReservaDto.agregados.listaExtra.forEach {
            agregados.listaExtra.add(extraService.get(it.id)!!)
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
        val catering = CateringEvento(
            eventoReservaDto.catering.id,
            eventoReservaDto.catering.presupuesto,
            eventoReservaDto.catering.cateringOtro,
            eventoReservaDto.catering.descripcion,
            mutableSetOf(),
            mutableSetOf())

        eventoReservaDto.catering.listaExtraTipoCatering.forEach{
            catering.listaTipoCatering.add(extraService.get(it.id)!!)
        }

        eventoReservaDto.catering.listaExtraCateringVariable.forEach{
            val extraVariable = CateringEventoExtraVariableCatering(0, extraService.get(it.id)!!, it.cantidad)
            catering.listaCateringExtraVariableCatering.add(extraVariable)
        }

        catering.listaCateringExtraVariableCatering.forEach{
            it.cateringEvento = catering
        }

        // Inicializacion Evento
        val evento = Evento(
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

        evento.agregados.evento = evento
        evento.catering.evento = evento

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

    @GetMapping("/getAllEstadoForSaveEvento")
    fun getAllEstadoForSaveEvento(): ResponseEntity<MutableSet<Estado>>? {
        return ResponseEntity<MutableSet<Estado>>(mutableSetOf(Estado.COTIZADO, Estado.RESERVADO), HttpStatus.OK)
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

    @GetMapping("/getEventoExtra/{id}")
    fun getEventoExtra(@PathVariable("id") id: Long): ResponseEntity<EventoExtraDto>? {
        val evento = eventoService.get(id)!!

        val agregados = AgregadosDto(evento.agregados.id,
            evento.agregados.extraOtro,
            evento.agregados.descuento,
            extraService.getListaExtraDto(evento.agregados.listaExtra, evento.inicio),
            extraService.getListaExtraVariableReservaDto(evento.agregados.listaEventoExtraVariable, evento.inicio))

        return ResponseEntity<EventoExtraDto>(EventoExtraDto(evento.id, evento.nombre, evento.codigo, evento.presupuesto,
            agregados, evento.tipoEvento.id, evento.inicio), HttpStatus.OK)
    }

    @GetMapping("/getEventoCatering/{id}")
    fun getEventoCatering(@PathVariable("id") id: Long): ResponseEntity<EventoCateringDto>? {
        val evento = eventoService.get(id)!!

        val catering = CateringEventoDto(evento.catering.id,
            evento.catering.cateringOtro,
            evento.catering.presupuesto,
            evento.catering.descripcion,
            extraService.getListaExtraDto(evento.catering.listaTipoCatering, evento.inicio),
            extraService.getListaExtraVariableReservaCateringDto(evento.catering.listaCateringExtraVariableCatering, evento.inicio))

        return ResponseEntity<EventoCateringDto>(EventoCateringDto(evento.id, evento.nombre, evento.codigo, catering,
            evento.tipoEvento.id, evento.inicio, evento.capacidad), HttpStatus.OK)
    }

    @GetMapping("/getEventoHora/{id}")
    fun getEventoHora(@PathVariable("id") id: Long): ResponseEntity<EventoHoraDto>? {
        val evento = eventoService.get(id)!!

        return ResponseEntity<EventoHoraDto>(EventoHoraDto(evento.id, evento.nombre, evento.codigo, evento.inicio, evento.fin), HttpStatus.OK)
    }

    @GetMapping("/getEventoVer/{id}")
    fun getEventoVer(@PathVariable("id") id: Long): ResponseEntity<EventoVerDto>? {
        val evento = eventoService.get(id)!!

        val agregados = AgregadosDto(evento.agregados.id,
            evento.agregados.extraOtro,
            evento.agregados.descuento,
            extraService.getListaExtraDto(evento.agregados.listaExtra, evento.inicio),
            extraService.getListaExtraVariableReservaDto(evento.agregados.listaEventoExtraVariable, evento.inicio))

        val catering = CateringEventoDto(evento.catering.id,
            evento.catering.cateringOtro,
            evento.catering.presupuesto,
            evento.catering.descripcion,
            extraService.getListaExtraDto(evento.catering.listaTipoCatering, evento.inicio),
            extraService.getListaExtraVariableReservaCateringDto(evento.catering.listaCateringExtraVariableCatering, evento.inicio))


        return ResponseEntity<EventoVerDto>(EventoVerDto(evento.id, evento.nombre, evento.codigo,
            evento.inicio,evento.fin,evento.tipoEvento.nombre,evento.capacidad,agregados,catering,
            evento.cliente,evento.presupuesto,evento.estado), HttpStatus.OK)
    }

    @PostMapping("/editEventoHora")
    fun editEventoHora(@RequestBody eventoHoraDto: EventoHoraDto): ResponseEntity<EventoHoraDto>? {
        val evento = eventoService.get(eventoHoraDto.id)!!

        evento.inicio = eventoHoraDto.inicio
        evento.fin = eventoHoraDto.fin

        eventoService.save(evento)

        return ResponseEntity<EventoHoraDto>(EventoHoraDto(evento.id, evento.nombre, evento.codigo, evento.inicio, evento.fin), HttpStatus.OK)
    }

    @PostMapping("/editEventoExtra")
    fun editEventoExtra(@RequestBody eventoExtraDto: EventoExtraDto): ResponseEntity<EventoHoraDto>? {
        val evento = eventoService.get(eventoExtraDto.id)!!

        //TODO No estaria eliminando...
        evento.agregados.listaEventoExtraVariable.forEach {
            eventoExtraVariableTipoEventoService.delete(it.id)
        }

        val listaExtra = mutableSetOf<Extra>()
        eventoExtraDto.agregados.listaExtra.forEach {
            listaExtra.add(extraService.get(it.id)!!)
        }

        val listaExtraVariable = mutableSetOf<EventoExtraVariableTipoEvento>()
        eventoExtraDto.agregados.listaExtraVariable.forEach{
            val extraVariable = EventoExtraVariableTipoEvento(0, extraService.get(it.id)!!, it.cantidad)
            listaExtraVariable.add(extraVariable)
        }

        listaExtraVariable.forEach { it.agregados = evento.agregados }

        evento.presupuesto = eventoExtraDto.presupuesto
        evento.agregados.listaExtra = listaExtra
        evento.agregados.listaEventoExtraVariable = listaExtraVariable
        evento.agregados.extraOtro = eventoExtraDto.agregados.extraOtro
        evento.agregados.descuento = eventoExtraDto.agregados.descuento

        eventoService.save(evento)

        return ResponseEntity<EventoHoraDto>(EventoHoraDto(evento.id, evento.nombre, evento.codigo, evento.inicio, evento.fin), HttpStatus.OK)
    }

    @PostMapping("/editEventoCatering")
    fun editEventoCatering(@RequestBody eventoCateringDto: EventoCateringDto): ResponseEntity<EventoHoraDto>? {
        val evento = eventoService.get(eventoCateringDto.id)!!

        evento.catering.listaTipoCatering = mutableSetOf()
        evento.catering.listaCateringExtraVariableCatering = mutableSetOf()

        eventoService.save(evento)

        val listaExtra = mutableSetOf<Extra>()
        eventoCateringDto.catering.listaExtraTipoCatering.forEach {
            listaExtra.add(extraService.get(it.id)!!)
        }

        val listaExtraVariable = mutableSetOf<CateringEventoExtraVariableCatering>()
        eventoCateringDto.catering.listaExtraCateringVariable.forEach{
            val extraVariable = CateringEventoExtraVariableCatering(0, extraService.get(it.id)!!, it.cantidad)
            listaExtraVariable.add(extraVariable)
        }

        listaExtraVariable.forEach { it.cateringEvento = evento.catering }

        evento.catering.listaTipoCatering = listaExtra
        evento.catering.listaCateringExtraVariableCatering = listaExtraVariable
        evento.catering.cateringOtro = eventoCateringDto.catering.cateringOtro
        evento.catering.descripcion = eventoCateringDto.catering.descripcion
        evento.catering.presupuesto = eventoCateringDto.catering.presupuesto

        eventoService.save(evento)

        return ResponseEntity<EventoHoraDto>(EventoHoraDto(evento.id, evento.nombre, evento.codigo, evento.inicio, evento.fin), HttpStatus.OK)
    }

    @PutMapping("/getListaEventoByDiaAndEmpresaId")
    fun getListaEventoByDiaAndEmpresaId(@RequestBody eventoBuscarFechaDto: EventoBuscarFechaDto): ResponseEntity<List<String>> {

        val listaEvento: List<Evento> = eventoService.findAllByInicioBetweenAndListaEmpresa(
            empresaService.get(eventoBuscarFechaDto.empresaId)!!, eventoBuscarFechaDto.desde, eventoBuscarFechaDto.hasta)

        val listaFecha: MutableList<String> = mutableListOf()

        if (listaEvento.isNotEmpty()) {
            for (evento in listaEvento) {
                val fecha = StringBuilder()

                // En caso de que sea el dia siguiente le agrega la fecha tambien no solo la hora
                if (evento.inicio.plusDays(1).dayOfMonth == evento.fin.dayOfMonth) {
                    fecha.append(
                        evento.inicio.toLocalTime().toString() + " hasta " + evento.fin.toLocalTime().toString() + " del dia " + evento.fin.toLocalDate().toString()
                    )
                } else {
                    fecha.append(evento.inicio.toLocalTime().toString() + " hasta " + evento.fin.toLocalTime().toString())
                }

                fecha.append(" (" + evento.tipoEvento.nombre + ")")
                listaFecha.add(fecha.toString())

                // Ordena la lista de mas temprano a mas tarde
                listaFecha.sort()
            }
        }
        return ResponseEntity<List<String>>(listaFecha, HttpStatus.OK)
    }

    @PutMapping("/horarioDisponible")
    fun horarioDisponible(@RequestBody eventoBuscarFechaDto: EventoBuscarFechaDto): ResponseEntity<Boolean> {

        val listaEvento: List<Evento> = eventoService.findAllByInicioBetweenAndListaEmpresa(
            empresaService.get(eventoBuscarFechaDto.empresaId)!!, eventoBuscarFechaDto.desde, eventoBuscarFechaDto.hasta)

        return ResponseEntity<Boolean>(eventoService.getHorarioDisponible(listaEvento, eventoBuscarFechaDto.desde, eventoBuscarFechaDto.hasta), HttpStatus.OK)
    }

}