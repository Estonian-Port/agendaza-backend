package com.estonianport.agendaza.controller

import com.estonianport.agendaza.common.emailService.EmailService
import com.estonianport.agendaza.common.openPDF.PdfService
import com.estonianport.agendaza.dto.*
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.*
import com.estonianport.agendaza.model.enums.Estado
import com.estonianport.agendaza.model.enums.TipoExtra
import com.estonianport.agendaza.service.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@CrossOrigin("*")
class EventoController {

    @Autowired
    lateinit var eventoService: EventoService

    @Autowired
    lateinit var empresaService: EmpresaService

    @Autowired
    lateinit var tipoEventoService: TipoEventoService

    @Autowired
    lateinit var capacidadService: CapacidadService

    @Autowired
    lateinit var extraService: ExtraService

    @Autowired
    lateinit var extraVariableService: ExtraVariableService

    @Autowired
    lateinit var usuarioService: UsuarioService

    @Autowired
    lateinit var emailService: EmailService

    @Autowired
    lateinit var pdfService: PdfService

    // TODO Sacar, no se va a usar, ya que se accede desde empresa
    @GetMapping("/getAllEvento")
    fun getAll(): List<EventoDTO>? {
        return eventoService.listaEventoToListaEventoDto(eventoService.getAll())
    }

    @GetMapping("/getEvento/{id}")
    fun get(@PathVariable("id") id: Long): Evento? {
        return eventoService.findById(id)
    }

    @GetMapping("/cantEventos/{id}")
    fun cantEventos(@PathVariable("id") id: Long) =  eventoService.contadorDeEventos(id)

    @GetMapping("/cantEventosFiltrados/{id}/{buscar}")
    fun cantEventosFiltrados(@PathVariable("id") id: Long, @PathVariable("buscar") buscar : String) =  eventoService.contadorDeEventosFiltrados(id,buscar)

    @PostMapping("/saveEvento")
    fun save(@RequestBody eventoReservaDto: EventoReservaDTO): Long {

        val empresa = empresaService.findById(eventoReservaDto.empresaId)

        // TODO Siempre es empty, sacar esto y mandar a fromEventoReservaDtoToEvento
        if (eventoReservaDto.codigo.isEmpty()) {
            eventoReservaDto.codigo = eventoService.generateCodigoForEventoOfEmpresa(empresa)
        }

        // TODO Capacidad evento, setear en el fromEventoReservaDtoToEvento por ahi, ver que es mejor
        eventoReservaDto.capacidad = capacidadService.reutilizarCapacidad(eventoReservaDto.capacidad)

        // Lista Extra y ExtraVariable
        val listaExtra = mutableSetOf<Extra>()
        val listaEventoExtraVariable = mutableSetOf<EventoExtraVariable>()

        // TODO Reducir el DTO para q venga todo unificado la listaExtra y ExtraVariable
        listaExtra.addAll(
            extraService.fromListaExtraDtoToListaExtra(
                eventoReservaDto.listaExtra
            )
        )

        listaExtra.addAll(
            extraService.fromListaExtraDtoToListaExtra(
                eventoReservaDto.listaExtraTipoCatering
            )
        )

        listaEventoExtraVariable.addAll(
            extraVariableService.fromListaExtraVariableDtoToListaExtraVariable(
                eventoReservaDto.listaExtraVariable
            )
        )

        listaEventoExtraVariable.addAll(
            extraVariableService.fromListaExtraVariableDtoToListaExtraVariable(
                eventoReservaDto.listaExtraCateringVariable
            )
        )

        // Inicializacion Evento
        val evento = eventoService.fromEventoReservaDtoToEvento(
            eventoReservaDto,
            tipoEventoService.get(eventoReservaDto.tipoEventoId)!!,
            listaExtra,
            listaEventoExtraVariable,
            usuarioService.findById(eventoReservaDto.encargadoId)!!,
            empresa
        )

        //TODO arreglar con cascade
        val cliente = eventoReservaDto.cliente

        evento.cliente = when {
            cliente.id != 0L -> usuarioService.get(cliente.id)!!
            cliente.email.isNotBlank() && usuarioService.existsByEmail(cliente.email) -> usuarioService.getUsuarioByEmail(cliente.email)!!
            usuarioService.existsByCelular(cliente.celular) -> usuarioService.getUsuarioByCelular(cliente.celular)!!
            else -> usuarioService.save(cliente)
        }

        val eventoSaved = eventoService.save(evento)

        evento.listaEventoExtraVariable.forEach {
            it.evento = eventoSaved
            extraVariableService.save(it)
        }

        try {
            if (emailService.isEmailValid(evento.cliente.email)) {
                // TODO mejorar el "Action" a un objeto que los tenga, Envia mail con comprobante
                emailService.enviarMailComprabanteReserva(evento, "sido reservado", empresa);
            }
        } catch (_: Exception) {
            // TODO enviar notificacion de fallo al enviar el mail
        }

        return eventoSaved.id
    }

    @DeleteMapping("/deleteEvento/{id}")
    fun delete(@PathVariable("id") id: Long): EventoDTO {
        val evento = eventoService.findById(id)
        evento.fechaBaja = LocalDate.now()

        return eventoService.save(evento).toDto()
    }

    @GetMapping("/getAllEstado")
    fun getAllEstado(): MutableSet<Estado>? {
        return Estado.values().toMutableSet()
    }

    @GetMapping("/getAllEstadoForSaveEvento")
    fun getAllEstadoForSaveEvento(): MutableSet<Estado>? {
        return mutableSetOf(Estado.COTIZADO, Estado.RESERVADO)
    }

    @GetMapping("/getEventoExtra/{id}")
    fun getEventoExtra(@PathVariable("id") id: Long): EventoExtraDTO? {
        val evento = eventoService.findById(id)
        val empresa = empresaService.get(evento.empresa.id)!!

        return evento.toEventoExtraDto(
            extraService.fromListaExtraToListaExtraDtoByFilter(
                    empresa,
                    evento.listaExtra,
                    evento.inicio,
                    TipoExtra.EVENTO
            ),
            extraVariableService.fromListaExtraVariableToListaExtraVariableDtoByFilter(
                    empresa,
                    evento.listaEventoExtraVariable,
                    evento.inicio,
                    TipoExtra.VARIABLE_EVENTO
            )
        )
    }

    @GetMapping("/getEventoCatering/{id}")
    fun getEventoCatering(@PathVariable("id") id: Long): EventoCateringDTO? {
        val evento = eventoService.findById(id)
        val empresa = empresaService.get(evento.empresa.id)!!

        return evento.toEventoCateringDto(
            extraService.fromListaExtraToListaExtraDtoByFilter(
                    empresa,
                    evento.listaExtra,
                    evento.inicio,
                    TipoExtra.TIPO_CATERING
            ),
            extraVariableService.fromListaExtraVariableToListaExtraVariableDtoByFilter(
                    empresa,
                    evento.listaEventoExtraVariable,
                    evento.inicio,
                    TipoExtra.VARIABLE_CATERING
            )
        )
    }

    @GetMapping("/getEventoHora/{id}")
    fun getEventoHora(@PathVariable("id") id: Long): EventoHoraDTO? {
        return eventoService.findById(id).toEventoHoraDto()
    }

    // TODO unificar EventoVerDto con EventoReservaDto
    @GetMapping("/getEventoVer/{id}")
    fun getEventoVer(@PathVariable("id") id: Long): EventoVerDTO? {
        val evento = eventoService.findById(id)
        val empresa = empresaService.get(evento.empresa.id)!!

        return evento.toEventoVerDto(
            extraService.fromListaExtraToListaExtraDtoByFilter(
                    empresa,
                    evento.listaExtra,
                    evento.inicio,
                    TipoExtra.EVENTO
            ),
            extraVariableService.fromListaExtraVariableToListaExtraVariableDtoByFilter(
                    empresa,
                    evento.listaEventoExtraVariable,
                    evento.inicio,
                    TipoExtra.VARIABLE_EVENTO
            ),
            extraService.fromListaExtraToListaExtraDtoByFilter(
                    empresa,
                    evento.listaExtra,
                    evento.inicio,
                    TipoExtra.TIPO_CATERING
            ),
            extraVariableService.fromListaExtraVariableToListaExtraVariableDtoByFilter(
                    empresa,
                    evento.listaEventoExtraVariable,
                    evento.inicio,
                    TipoExtra.VARIABLE_CATERING
            )
        )
    }

    @PostMapping("/editEventoHora")
    fun editEventoHora(@RequestBody eventoHoraDto: EventoHoraDTO): EventoHoraDTO? {
        val evento = eventoService.findById(eventoHoraDto.id)

        evento.inicio = eventoHoraDto.inicio
        evento.fin = eventoHoraDto.fin

        eventoService.save(evento)

        return evento.toEventoHoraDto()
    }

    //TODO Refactorizar
    @PostMapping("/editEventoExtra")
    fun editEventoExtra(@RequestBody eventoExtraDto: EventoExtraDTO): Long? {
        val evento = eventoService.findById(eventoExtraDto.id)

        val listaExtra = mutableSetOf<Extra>()
        listaExtra.addAll(
            extraService.fromListaExtraDtoToListaExtra(
                eventoExtraDto.listaExtra.toList()
            )
        )

        // TODO revisar el delete
        // Elimina la lista de extraVariable que sean variable evento y no catering
        evento.listaEventoExtraVariable.filter { it.extra.tipoExtra == TipoExtra.VARIABLE_EVENTO }.forEach {
            extraVariableService.delete(it.id)
        }

        // Seteo listaExtraVariable
        val listaEventoExtraVariable = mutableSetOf<EventoExtraVariable>()
        listaEventoExtraVariable.addAll(
            extraVariableService.fromListaExtraVariableDtoToListaExtraVariable(
                eventoExtraDto.listaExtraVariable.toList()
            )
        )

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
    fun editEventoCatering(@RequestBody eventoCateringDto: EventoCateringDTO): EventoHoraDTO? {
        val evento = eventoService.findById(eventoCateringDto.id)

        val listaExtra = mutableSetOf<Extra>()
        listaExtra.addAll(
            extraService.fromListaExtraDtoToListaExtra(
                eventoCateringDto.listaExtraTipoCatering.toList()
            )
        )

        // TODO revisar el delete
        // Elimina la lista de extraVariable
        evento.listaEventoExtraVariable.filter { it.extra.tipoExtra == TipoExtra.VARIABLE_CATERING }.forEach {
            extraVariableService.delete(it.id)
        }

        // Seteo listaExtraVariable
        val listaEventoExtraVariable = mutableSetOf<EventoExtraVariable>()
        listaEventoExtraVariable.addAll(
            extraVariableService.fromListaExtraVariableDtoToListaExtraVariable(
                eventoCateringDto.listaExtraCateringVariable.toList()
            )
        )

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

        return EventoHoraDTO(evento.id, evento.nombre, evento.codigo, evento.inicio, evento.fin)
    }

    @PutMapping("/getListaEventoByDiaAndEmpresaId")
    fun getListaEventoByDiaAndEmpresaId(@RequestBody eventoBuscarFechaDto: EventoBuscarFechaDTO): List<String> {

        val listaEvento: List<Evento> = eventoService.findAllByInicioBetweenAndListaEmpresa(
            empresaService.findById(eventoBuscarFechaDto.empresaId), eventoBuscarFechaDto.desde, eventoBuscarFechaDto.hasta
        )

        val listaFecha: MutableList<String> = mutableListOf()

        if (listaEvento.isNotEmpty()) {
            for (evento in listaEvento) {
                val fecha = StringBuilder()

                // En caso de que sea el dia siguiente le agrega la fecha tambien no solo la hora
                if (evento.inicio.plusDays(1).dayOfMonth == evento.fin.dayOfMonth) {
                    fecha.append(
                        evento.inicio.toLocalTime().toString() + " hasta " + evento.fin.toLocalTime()
                            .toString() + " del dia " + evento.fin.toLocalDate().toString()
                    )
                } else {
                    fecha.append(
                        evento.inicio.toLocalTime().toString() + " hasta " + evento.fin.toLocalTime().toString()
                    )
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
    fun horarioDisponible(@RequestBody eventoBuscarFechaDto: EventoBuscarFechaDTO): Boolean {

        val listaEvento: List<Evento> = eventoService.findAllByInicioBetweenAndListaEmpresa(
            empresaService.findById(eventoBuscarFechaDto.empresaId), eventoBuscarFechaDto.desde, eventoBuscarFechaDto.hasta
        )

        return eventoService.getHorarioDisponible(listaEvento, eventoBuscarFechaDto.desde, eventoBuscarFechaDto.hasta)
    }

    @PutMapping("/reenviarMail/{id}")
    fun reenviarMail(@PathVariable("id") id: Long, @RequestBody empresaId: Long): Boolean {

        try {
            val evento = eventoService.findById(id)
            val empresa = empresaService.findById(empresaId)

            emailService.enviarMailComprabanteReserva(evento, "sido reservado (reenvio)", empresa)
            return true
        } catch (e: Exception) {
            throw NotFoundException("No se pudo reenviar mail")
        }
    }

    @PostMapping("/editEventoAnotaciones/{id}")
    fun editEventoAnotaciones(@PathVariable("id") id: Long, @RequestBody anotaciones: String): String {
        val evento = eventoService.findById(id)
        evento.anotaciones = anotaciones

        return eventoService.save(evento).anotaciones
    }

    @PostMapping("/editEventoCantidadAdultos")
    fun editEventoCantidadAdultos(@RequestBody eventoDto: EventoVerDTO): Int {
        val evento = eventoService.findById(eventoDto.id)
        evento.capacidad.capacidadAdultos  = eventoDto.capacidad.capacidadAdultos

        return eventoService.save(evento).capacidad.capacidadAdultos
    }

    @PostMapping("/editEventoCantidadNinos")
    fun editEventoCantidadNinos(@RequestBody eventoDto: EventoVerDTO): Int {
        val evento = eventoService.findById(eventoDto.id)
        evento.capacidad.capacidadNinos = eventoDto.capacidad.capacidadNinos

        return eventoService.save(evento).capacidad.capacidadNinos
    }

    @PostMapping("/editEventoNombre/{id}")
    fun editEventoCantidadNombre(@PathVariable("id") id: Long, @RequestBody nombre: String): String {
        val evento = eventoService.findById(id)
        evento.nombre = nombre

        return eventoService.save(evento).nombre
    }

    @GetMapping("/getPresupuesto/{id}")
    fun editEventoCantidadNombre(@PathVariable("id") id: Long): Double {
        return eventoService.findById(id).getPresupuestoTotal()
    }

    @PutMapping("/getEventosByUsuarioAndEmpresa")
    fun getEventosByUsuarioIdAndEmpresaId(@RequestBody usuarioEmpresaDto : UsuarioEmpresaDTO): List<EventoUsuarioDTO> {
        return eventoService.getEventosByUsuarioIdAndEmpresaId(usuarioEmpresaDto).map { it.toEventoUsuarioDto(it) }
    }

    @PutMapping("/getCantEventosByUsuarioAndEmpresa")
    fun getCantEventosByUsuarioIdAndEmpresaId(@RequestBody usuarioEmpresaDto : UsuarioEmpresaDTO): Int {
        return eventoService.getCantEventosByUsuarioIdAndEmpresaId(usuarioEmpresaDto)
    }

    @PutMapping("/recorrerEspecificaciones/{id}")
    fun recorrerEspecificaciones(@PathVariable("id") empresaId : Long, @RequestBody eventoReservaDto : EventoReservaDTO): EventoReservaDTO {
        val empresa =  empresaService.get(empresaId)!!
        val tipoEvento = tipoEventoService.get(eventoReservaDto.tipoEventoId)!!
        val listaExtra = mutableSetOf<Extra>()

        listaExtra.addAll(
                extraService.fromListaExtraDtoToListaExtra(
                        eventoReservaDto.listaExtraTipoCatering.toList()
                )
        )

        val listaEventoExtraVariable = mutableSetOf<EventoExtraVariable>()
        listaEventoExtraVariable.addAll(
                extraVariableService.fromListaExtraVariableDtoToListaExtraVariable(
                        eventoReservaDto.listaExtraCateringVariable.toList()
                )
        )

        val encargado =  usuarioService.findById(eventoReservaDto.encargadoId)!!

        val evento = eventoService.fromEventoReservaDtoToEvento(eventoReservaDto, tipoEvento, listaExtra, listaEventoExtraVariable, encargado,  empresa)

        empresa.recorrerEspecificaciones(evento)

        return evento.toEventoReservaDto(
                extraService.fromListaExtraToListaExtraDtoByFilter(
                        empresa,
                        evento.listaExtra,
                        evento.inicio,
                        TipoExtra.EVENTO
                ),
                extraVariableService.fromListaExtraVariableToListaExtraVariableDtoByFilter(
                        empresa,
                        evento.listaEventoExtraVariable,
                        evento.inicio,
                        TipoExtra.VARIABLE_EVENTO
                ),
                extraService.fromListaExtraToListaExtraDtoByFilter(
                        empresa,
                        evento.listaExtra,
                        evento.inicio,
                        TipoExtra.TIPO_CATERING
                ),
                extraVariableService.fromListaExtraVariableToListaExtraVariableDtoByFilter(
                        empresa,
                        evento.listaEventoExtraVariable,
                        evento.inicio,
                        TipoExtra.VARIABLE_CATERING
                ))
    }

    @GetMapping("/generarEstadoDeCuenta/{id}")
    fun generarEstadoDeCuenta(@PathVariable("id") id: Long): ResponseEntity<ByteArray> {
        val pago = eventoService.get(id)!!
        val pdfBytes = pdfService.generarEstadoDeCuenta(pago)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_PDF
        headers.setContentDispositionFormData("attachment", "estado_de_cuenta.pdf")

        return ResponseEntity(pdfBytes, headers, HttpStatus.OK)
    }

    @GetMapping("/descargarEvento/{id}")
    fun descargarEvento(@PathVariable("id") id: Long): ResponseEntity<ByteArray> {
        val pago = eventoService.get(id)!!
        val pdfBytes = pdfService.generarComprobanteEvento(pago)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_PDF
        headers.setContentDispositionFormData("attachment", "comprobante_de_evento.pdf")

        return ResponseEntity(pdfBytes, headers, HttpStatus.OK)
    }
}