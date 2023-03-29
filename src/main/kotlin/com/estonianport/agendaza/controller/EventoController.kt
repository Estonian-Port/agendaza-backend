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
import com.estonianport.agendaza.dto.PagoDto
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

    @GetMapping("/getAllEvento")
    fun getAll(): MutableList<Evento>? {
        return eventoService.getAll()
    }

    @GetMapping("/getEvento/{id}")
    fun get(@PathVariable("id") id: Long): Evento? {
        return eventoService.get(id)
    }

    @PostMapping("/saveEvento")
    fun save(@RequestBody eventoReservaDto: EventoReservaDto): Long {

        val empresa : Empresa = empresaService.get(eventoReservaDto.empresaId)!!
        val tipoEvento : TipoEvento = tipoEventoService.get(eventoReservaDto.tipoEventoId)!!
        val encargado : Usuario = usuarioService.get(eventoReservaDto.encargadoId)!!

        // Generar codigo de reserva
        if(eventoReservaDto.codigo.isEmpty()){
            eventoReservaDto.codigo = eventoService.generateCodigoForEventoOfEmpresa(empresa)
        }

        // Capacidad evento
        eventoReservaDto.capacidad = capacidadService.reutilizarCapacidad(eventoReservaDto.capacidad)

        // Lista Extra y ExtraVariable
        val listaExtra = mutableSetOf<Extra>()
        val listaEventoExtraVariable = mutableSetOf<EventoExtraVariable>()

        eventoReservaDto.agregados.listaExtra.forEach {
            listaExtra.add(extraService.get(it.id)!!)
        }

        eventoReservaDto.agregados.listaExtraVariable.forEach {
            val eventoExtraVariable = EventoExtraVariable(0, extraService.get(it.id)!! ,it.cantidad)
            listaEventoExtraVariable.add(eventoExtraVariable)
        }

        eventoReservaDto.catering.listaExtraTipoCatering.forEach{
            listaExtra.add(extraService.get(it.id)!!)
        }

        eventoReservaDto.catering.listaExtraCateringVariable.forEach{
            val eventoExtraVariable = EventoExtraVariable(0, extraService.get(it.id)!!, it.cantidad)
            listaEventoExtraVariable.add(eventoExtraVariable)
        }

        // Inicializacion Evento
        val evento = Evento(
            eventoReservaDto.id,
            eventoReservaDto.nombre,
            tipoEvento,
            eventoReservaDto.inicio,
            eventoReservaDto.fin,
            eventoReservaDto.capacidad,
            eventoReservaDto.agregados.extraOtro,
            eventoReservaDto.agregados.descuento,
            listaExtra,
            listaEventoExtraVariable,
            eventoReservaDto.catering.cateringOtro,
            eventoReservaDto.catering.descripcion,
            encargado,
            eventoReservaDto.cliente,
            eventoReservaDto.codigo,
            eventoReservaDto.estado)


        // vincula el evento a la empresa
        evento.listaEmpresa.add(empresa)

        val eventoSave = eventoService.save(evento)

        evento.listaEventoExtraVariable.forEach {
            it.evento = eventoSave
            extraVariableService.save(it)
        }

        // TODO mejorar el "Action" a un objeto que los tenga, Envia mail con comprobante
        emailService.enviarMailComprabanteReserva(evento, "sido reservado", empresa);

        return eventoSave.id
    }

    @DeleteMapping("/deleteEvento/{id}")
    fun delete(@PathVariable("id") id: Long): ResponseEntity<Evento> {
        val evento = eventoService.get(id)!!

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
        val total = evento.getPresupuesto() + evento.getPresupuestoCatering()
        val listaPago: MutableSet<PagoDto> = mutableSetOf()

        // TODO pasar a DTO
        evento.listaPago.forEach {
            listaPago.add(PagoDto(it.id, it.monto, it.evento.codigo, it.medioDePago, it.evento.nombre, it.fecha))
        }

        return EventoPagoDto(evento.id, evento.nombre, evento.codigo, total, listaPago)
    }

    @GetMapping("/getEventoExtra/{id}")
    fun getEventoExtra(@PathVariable("id") id: Long): EventoExtraDto? {
        val evento = eventoService.get(id)!!

        // TODO Sacar Agregados
        val agregados = AgregadosDto(0,
            evento.extraOtro,
            evento.descuento,
            extraService.getListaExtraDto(evento.listaExtra.filter { it.tipoExtra == TipoExtra.EVENTO }.toMutableSet(), evento.inicio),
            extraService.getListaExtraVariableReservaDto(evento.listaEventoExtraVariable.filter { it.extra.tipoExtra == TipoExtra.VARIABLE_EVENTO }.toMutableSet(), evento.inicio))

        return EventoExtraDto(evento.id, evento.nombre, evento.codigo, evento.getPresupuesto(),
            agregados, evento.tipoEvento.id, evento.inicio)
    }

    @GetMapping("/getEventoCatering/{id}")
    fun getEventoCatering(@PathVariable("id") id: Long): EventoCateringDto? {
        val evento = eventoService.get(id)!!

        val catering = CateringEventoDto(0,
            evento.cateringOtro,
            evento.getPresupuestoCatering(),
            evento.cateringOtroDescripcion,
            extraService.getListaExtraDto(evento.listaExtra.filter { it.tipoExtra == TipoExtra.TIPO_CATERING }.toMutableSet(), evento.inicio),
            extraService.getListaExtraVariableReservaDto(evento.listaEventoExtraVariable.filter { it.extra.tipoExtra == TipoExtra.VARIABLE_CATERING }.toMutableSet(), evento.inicio))

        return EventoCateringDto(evento.id, evento.nombre, evento.codigo, catering,
            evento.tipoEvento.id, evento.inicio, evento.capacidad)
    }

    @GetMapping("/getEventoHora/{id}")
    fun getEventoHora(@PathVariable("id") id: Long): EventoHoraDto? {
        val evento = eventoService.get(id)!!

        return EventoHoraDto(evento.id, evento.nombre, evento.codigo, evento.inicio, evento.fin)
    }

    @GetMapping("/getEventoVer/{id}")
    fun getEventoVer(@PathVariable("id") id: Long): EventoVerDto? {
        val evento = eventoService.get(id)!!

        val agregados = AgregadosDto(0,
            evento.extraOtro,
            evento.descuento,
            extraService.getListaExtraDto(evento.listaExtra.filter { it.tipoExtra == TipoExtra.EVENTO }.toMutableSet(), evento.inicio),
            extraService.getListaExtraVariableReservaDto(evento.listaEventoExtraVariable.filter { it.extra.tipoExtra == TipoExtra.VARIABLE_EVENTO }.toMutableSet(), evento.inicio))

        val catering = CateringEventoDto(0,
            evento.cateringOtro,
            evento.getPresupuestoCatering(),
            evento.cateringOtroDescripcion,
            extraService.getListaExtraDto(evento.listaExtra.filter { it.tipoExtra == TipoExtra.TIPO_CATERING }.toMutableSet(), evento.inicio),
            extraService.getListaExtraVariableReservaDto(evento.listaEventoExtraVariable.filter { it.extra.tipoExtra == TipoExtra.VARIABLE_CATERING }.toMutableSet(), evento.inicio))


        return EventoVerDto(evento.id, evento.nombre, evento.codigo, evento.inicio,
            evento.fin,evento.tipoEvento.nombre,evento.capacidad,agregados,catering,
            evento.cliente,evento.getPresupuesto(),evento.estado)
    }

    @PostMapping("/editEventoHora")
    fun editEventoHora(@RequestBody eventoHoraDto: EventoHoraDto): EventoHoraDto? {
        val evento = eventoService.get(eventoHoraDto.id)!!

        evento.inicio = eventoHoraDto.inicio
        evento.fin = eventoHoraDto.fin

        eventoService.save(evento)

        return EventoHoraDto(evento.id, evento.nombre, evento.codigo, evento.inicio, evento.fin)
    }

    @PostMapping("/editEventoExtra")
    fun editEventoExtra(@RequestBody eventoExtraDto: EventoExtraDto): Long? {
        val evento = eventoService.get(eventoExtraDto.id)!!

        // Seteo listaExtra
        val listaExtra = mutableSetOf<Extra>()
        eventoExtraDto.agregados.listaExtra.forEach {
            listaExtra.add(extraService.get(it.id)!!)
        }

        // Elimina la lista de extraVariable
        evento.listaEventoExtraVariable.filter { it.extra.tipoExtra == TipoExtra.VARIABLE_EVENTO }.forEach {
            extraVariableService.delete(it.id)
        }

        // Seteo listaExtraVariable
        val listaEventoExtraVariable = mutableSetOf<EventoExtraVariable>()
        eventoExtraDto.agregados.listaExtraVariable.forEach{
            val eventoExtraVariable = EventoExtraVariable(0, extraService.get(it.id)!!, it.cantidad)
            listaEventoExtraVariable.add(eventoExtraVariable)
        }

        // Guarda la lista de extraVariable
        listaEventoExtraVariable.forEach {
            extraVariableService.save(it)
        }

        evento.listaExtra = listaExtra
        evento.listaEventoExtraVariable = listaEventoExtraVariable
        evento.extraOtro = eventoExtraDto.agregados.extraOtro
        evento.descuento = eventoExtraDto.agregados.descuento

        eventoService.save(evento)

        return evento.id
    }

    @PostMapping("/editEventoCatering")
    fun editEventoCatering(@RequestBody eventoCateringDto: EventoCateringDto): EventoHoraDto? {
        val evento = eventoService.get(eventoCateringDto.id)!!

        // Seteo listaExtra
        val listaExtra = mutableSetOf<Extra>()
        eventoCateringDto.catering.listaExtraTipoCatering.forEach {
            listaExtra.add(extraService.get(it.id)!!)
        }

        // Elimina la lista de extraVariable
        evento.listaEventoExtraVariable.filter { it.extra.tipoExtra == TipoExtra.VARIABLE_CATERING }.forEach {
            extraVariableService.delete(it.id)
        }

        // Seteo listaExtraVariable
        val listaEventoExtraVariable = mutableSetOf<EventoExtraVariable>()
        eventoCateringDto.catering.listaExtraCateringVariable.forEach{
            val eventoExtraVariable = EventoExtraVariable(0, extraService.get(it.id)!!, it.cantidad)
            listaEventoExtraVariable.add(eventoExtraVariable)
        }

        // Guarda la lista de extraVariable
        listaEventoExtraVariable.forEach {
            extraVariableService.save(it)
        }

        evento.listaExtra = listaExtra
        evento.listaEventoExtraVariable = listaEventoExtraVariable
        evento.cateringOtro = eventoCateringDto.catering.cateringOtro
        evento.cateringOtroDescripcion = eventoCateringDto.catering.descripcion

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