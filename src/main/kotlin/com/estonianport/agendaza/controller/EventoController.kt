package com.estonianport.agendaza.controller

import com.estonianport.agendaza.common.emailService.EmailService
import com.estonianport.agendaza.dto.AgregadosDto
import com.estonianport.agendaza.dto.CateringEventoDto
import com.estonianport.agendaza.dto.EventoBuscarFechaDto
import com.estonianport.agendaza.dto.EventoCateringDto
import com.estonianport.agendaza.dto.EventoExtraDto
import com.estonianport.agendaza.dto.EventoHoraDto
import com.estonianport.agendaza.dto.EventoPagoDto
import com.estonianport.agendaza.dto.EventoReservaDto
import com.estonianport.agendaza.dto.EventoVerDto
import com.estonianport.agendaza.errors.BusinessException
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.Capacidad
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Estado
import com.estonianport.agendaza.model.Evento
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.EventoExtraVariable
import com.estonianport.agendaza.model.TipoEvento
import com.estonianport.agendaza.model.TipoExtra
import com.estonianport.agendaza.model.Usuario
import com.estonianport.agendaza.service.CapacidadService
import com.estonianport.agendaza.service.EmpresaService
import com.estonianport.agendaza.service.EventoService
import com.estonianport.agendaza.service.ExtraService
import com.estonianport.agendaza.service.ExtraVariableService
import com.estonianport.agendaza.service.PagoService
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
    lateinit var extraVariableService : ExtraVariableService

    @Autowired
    lateinit var usuarioService : UsuarioService

    @Autowired
    lateinit var emailService : EmailService

    @Autowired
    lateinit var pagoService : PagoService

    @GetMapping("/getAllEvento")
    fun getAll(): MutableList<Evento>? {
        return eventoService.getAll()
    }

    @GetMapping("/getEvento/{id}")
    fun get(@PathVariable("id") id: Long): Evento? {
        return eventoService.get(id)
    }

    //TODO refactor sacar agregados y catering de eventoReservaDto
    @PostMapping("/saveEvento")
    fun save(@RequestBody eventoReservaDto: EventoReservaDto): Long {

        val empresa = empresaService.get(eventoReservaDto.empresaId)!!
        val tipoEvento = tipoEventoService.get(eventoReservaDto.tipoEventoId)!!
        val encargado = usuarioService.get(eventoReservaDto.encargadoId)!!

        // Generar codigo de reserva
        if(eventoReservaDto.codigo.isEmpty()){
            eventoReservaDto.codigo = eventoService.generateCodigoForEventoOfEmpresa(empresa)
        }

        // Capacidad evento
        eventoReservaDto.capacidad = capacidadService.reutilizarCapacidad(eventoReservaDto.capacidad)

        // Lista Extra y ExtraVariable
        val listaExtra = mutableSetOf<Extra>()
        val listaEventoExtraVariable = mutableSetOf<EventoExtraVariable>()

        //TODO Reducir el DTO para q venga todo unificado la listaExtra y ExtraVariable
        listaExtra.addAll(
            extraService.fromListaExtraDtoToListaExtra(
                eventoReservaDto.listaExtra))

        listaExtra.addAll(
            extraService.fromListaExtraDtoToListaExtra(
                eventoReservaDto.listaExtraTipoCatering))

        listaEventoExtraVariable.addAll(
            extraVariableService.fromListaExtraVariableDtoToListaExtraVariable(
                eventoReservaDto.listaExtraVariable))

        listaEventoExtraVariable.addAll(
            extraVariableService.fromListaExtraVariableDtoToListaExtraVariable(
                eventoReservaDto.listaExtraCateringVariable))

        // Inicializacion Evento
        val evento = eventoService.fromEventoReservaDtoToEvento(
            eventoReservaDto, tipoEvento, listaExtra, listaEventoExtraVariable, encargado)

        // vincula el evento a la empresa
        evento.listaEmpresa.add(empresa)

        val eventoSaved = eventoService.save(evento)

        evento.listaEventoExtraVariable.forEach {
            it.evento = eventoSaved
            extraVariableService.save(it)
        }

        try {
            if(evento.cliente.email.isNotEmpty()) {
                // TODO mejorar el "Action" a un objeto que los tenga, Envia mail con comprobante
                emailService.enviarMailComprabanteReserva(evento, "sido reservado", empresa);
            }
        }catch (_: BusinessException){
            // TODO enviar notificacion de fallo al enviar el mail
        }

        return eventoSaved.id
    }

    @DeleteMapping("/deleteEvento/{id}")
    fun delete(@PathVariable("id") id: Long): ResponseEntity<Evento> {
        val evento = eventoService.get(id)!!

        // TODO Delegar a service
        evento.listaEventoExtraVariable.forEach {
            extraVariableService.delete(it.id)
        }

        evento.capacidad = Capacidad(0,0,0)

        eventoService.delete(id)
        return ResponseEntity<Evento>(HttpStatus.OK)
    }

    @GetMapping("/getAllEstado")
    fun getAllEstado(): MutableSet<Estado>? {
        return Estado.values().toMutableSet()
    }

    @GetMapping("/getAllEstadoForSaveEvento")
    fun getAllEstadoForSaveEvento(): MutableSet<Estado>? {
        return mutableSetOf(Estado.COTIZADO, Estado.RESERVADO)
    }

    @GetMapping("/getEventoPago/{id}")
    fun getEventoPago(@PathVariable("id") id: Long): EventoPagoDto? {
        val evento = eventoService.get(id)!!

        return evento.toEventoPagoDto(
            pagoService.fromListaPagoToListaPagoDto(evento.listaPago)
        )
    }

    @GetMapping("/getEventoExtra/{id}")
    fun getEventoExtra(@PathVariable("id") id: Long): EventoExtraDto? {
        val evento = eventoService.get(id)!!

        return evento.toEventoExtraDto(
            extraService.fromListaExtraToListaExtraDtoByFilter(evento.listaExtra, evento.inicio, TipoExtra.EVENTO),
            extraVariableService.fromListaExtraVariableToListaExtraVariableDtoByFilter(evento.listaEventoExtraVariable, evento.inicio, TipoExtra.VARIABLE_EVENTO)
        )
    }

    @GetMapping("/getEventoCatering/{id}")
    fun getEventoCatering(@PathVariable("id") id: Long): EventoCateringDto? {
        val evento = eventoService.get(id)!!

        return evento.toEventoCateringDto(
            extraService.fromListaExtraToListaExtraDtoByFilter(evento.listaExtra, evento.inicio, TipoExtra.TIPO_CATERING),
            extraVariableService.fromListaExtraVariableToListaExtraVariableDtoByFilter(evento.listaEventoExtraVariable, evento.inicio, TipoExtra.VARIABLE_CATERING)
        )
    }

    @GetMapping("/getEventoHora/{id}")
    fun getEventoHora(@PathVariable("id") id: Long): EventoHoraDto? {
        return eventoService.get(id)!!.toEventoHoraDto()
    }

    // TODO unificar EventoVerDto con EventoReservaDto
    @GetMapping("/getEventoVer/{id}")
    fun getEventoVer(@PathVariable("id") id: Long): EventoVerDto? {
        val evento = eventoService.get(id)!!

        return evento.toEventoVerDto(
            extraService.fromListaExtraToListaExtraDtoByFilter(evento.listaExtra, evento.inicio, TipoExtra.EVENTO),
            extraVariableService.fromListaExtraVariableToListaExtraVariableDtoByFilter(evento.listaEventoExtraVariable, evento.inicio, TipoExtra.VARIABLE_EVENTO),
            extraService.fromListaExtraToListaExtraDtoByFilter(evento.listaExtra, evento.inicio, TipoExtra.TIPO_CATERING),
            extraVariableService.fromListaExtraVariableToListaExtraVariableDtoByFilter(evento.listaEventoExtraVariable, evento.inicio, TipoExtra.VARIABLE_CATERING)
        )
    }

    @PostMapping("/editEventoHora")
    fun editEventoHora(@RequestBody eventoHoraDto: EventoHoraDto): EventoHoraDto? {
        val evento = eventoService.get(eventoHoraDto.id)!!

        evento.inicio = eventoHoraDto.inicio
        evento.fin = eventoHoraDto.fin

        eventoService.save(evento)

        return evento.toEventoHoraDto()
    }

    //TODO Refactorizar
    @PostMapping("/editEventoExtra")
    fun editEventoExtra(@RequestBody eventoExtraDto: EventoExtraDto): Long? {
        val evento = eventoService.get(eventoExtraDto.id)!!

        val listaExtra = mutableSetOf<Extra>()
        listaExtra.addAll(
            extraService.fromListaExtraDtoToListaExtra(
                eventoExtraDto.listaExtra.toList()))

        // TODO revisar el delete
        // Elimina la lista de extraVariable que sean variable evento y no catering
        evento.listaEventoExtraVariable.filter{ it.extra.tipoExtra == TipoExtra.VARIABLE_EVENTO }.forEach {
            extraVariableService.delete(it.id)
        }

        // Seteo listaExtraVariable
        val listaEventoExtraVariable = mutableSetOf<EventoExtraVariable>()
        listaEventoExtraVariable.addAll(
            extraVariableService.fromListaExtraVariableDtoToListaExtraVariable(
                eventoExtraDto.listaExtraVariable.toList()))

        // Guarda la lista de extraVariable
        listaEventoExtraVariable.forEach {
            it.evento = evento
            extraVariableService.save(it)
        }

        // Agrega a la lista los extras catering que no deben de ser modificados
        listaExtra.addAll(evento.listaExtra.filter { it.tipoExtra == TipoExtra.TIPO_CATERING })
        evento.listaExtra = listaExtra

        // Agrega a la lista los extras variables catering que no deben de ser modificados
        listaEventoExtraVariable.addAll(evento.listaEventoExtraVariable.filter { it.extra.tipoExtra == TipoExtra.VARIABLE_CATERING })
        evento.listaEventoExtraVariable = listaEventoExtraVariable

        evento.extraOtro = eventoExtraDto.extraOtro
        evento.descuento = eventoExtraDto.descuento

        eventoService.save(evento)

        return evento.id
    }

    //TODO Refactorizar
    @PostMapping("/editEventoCatering")
    fun editEventoCatering(@RequestBody eventoCateringDto: EventoCateringDto): EventoHoraDto? {
        val evento = eventoService.get(eventoCateringDto.id)!!

        val listaExtra = mutableSetOf<Extra>()
        listaExtra.addAll(
            extraService.fromListaExtraDtoToListaExtra(
                eventoCateringDto.listaExtraTipoCatering.toList()))

        // TODO revisar el delete
        // Elimina la lista de extraVariable
        evento.listaEventoExtraVariable.filter { it.extra.tipoExtra == TipoExtra.VARIABLE_CATERING }.forEach {
            extraVariableService.delete(it.id)
        }

        // Seteo listaExtraVariable
        val listaEventoExtraVariable = mutableSetOf<EventoExtraVariable>()
        listaEventoExtraVariable.addAll(
            extraVariableService.fromListaExtraVariableDtoToListaExtraVariable(
                eventoCateringDto.listaExtraCateringVariable.toList()))

        // Guarda la lista de extraVariable
        listaEventoExtraVariable.forEach {
            it.evento = evento
            extraVariableService.save(it)
        }

        // Agrega a la lista los extras evento que no deben de ser modificados
        listaExtra.addAll(evento.listaExtra.filter { it.tipoExtra == TipoExtra.EVENTO })
        evento.listaExtra = listaExtra

        // Agrega a la lista los extras variables evento que no deben de ser modificados
        listaEventoExtraVariable.addAll(evento.listaEventoExtraVariable.filter { it.extra.tipoExtra == TipoExtra.VARIABLE_EVENTO })
        evento.listaEventoExtraVariable = listaEventoExtraVariable

        evento.cateringOtro = eventoCateringDto.cateringOtro
        evento.cateringOtroDescripcion = eventoCateringDto.cateringOtroDescripcion

        eventoService.save(evento)

        return EventoHoraDto(evento.id, evento.nombre, evento.codigo, evento.inicio, evento.fin)
    }

    @PutMapping("/getListaEventoByDiaAndEmpresaId")
    fun getListaEventoByDiaAndEmpresaId(@RequestBody eventoBuscarFechaDto: EventoBuscarFechaDto): List<String> {

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
        return listaFecha
    }

    @PutMapping("/horarioDisponible")
    fun horarioDisponible(@RequestBody eventoBuscarFechaDto: EventoBuscarFechaDto): Boolean {

        val listaEvento: List<Evento> = eventoService.findAllByInicioBetweenAndListaEmpresa(
            empresaService.get(eventoBuscarFechaDto.empresaId)!!, eventoBuscarFechaDto.desde, eventoBuscarFechaDto.hasta)

        return eventoService.getHorarioDisponible(listaEvento, eventoBuscarFechaDto.desde, eventoBuscarFechaDto.hasta)
    }

    // TODO revisar el return true
    @PutMapping("/reenviarMail/{id}")
    fun reenviarMail(@PathVariable("id") id: Long, @RequestBody empresaId: Long): Boolean {

        try{
            val evento = eventoService.get(id)!!
            val empresa = empresaService.get(empresaId)!!

            emailService.enviarMailComprabanteReserva(evento, "sido reservado (reenvio)", empresa)
            return true
        }catch (e : Exception){
            throw NotFoundException("No se pudo reenviar mail")
        }
    }

}