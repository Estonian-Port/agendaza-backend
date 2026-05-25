package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.common.codeGeneratorUtil.CodeGeneratorUtil
import com.estonianport.agendaza.dto.*
import com.estonianport.agendaza.repository.EventoRepository
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Evento
import com.estonianport.agendaza.model.EventoExtraVariable
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.TipoEvento
import com.estonianport.agendaza.model.Usuario
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.CollectionUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors
import java.util.stream.IntStream
import kotlin.collections.ArrayList

@Service
class EventoService : GenericServiceImpl<Evento, Long>(
    private val eventoRepository: EventoRepository,
    private val empresaService: EmpresaService,
    private val capacidadService: CapacidadService,
    private val tipoEventoService: TipoEventoService,
    private val extraService: ExtraService,
    private val extraVariableService: ExtraVariableService,
    private val usuarioService: UsuarioService,
    private val emailService: EmailService) {

    @Autowired
    lateinit var eventoRepository: EventoRepository

    override val dao: CrudRepository<Evento, Long>
        get() = eventoRepository

    @Cacheable(value = ["agendaEventos"], key = "#id")
    fun getAllEventosForAgendaByEmpresaId(id : Long) : List<EventoAgendaDTO>{
        val hace2Meses = LocalDateTime.now().minusMonths(2)
        return eventoRepository.getAllEventosForAgendaByEmpresaId(id, hace2Meses)
    }

    fun getEventosByUsuarioIdAndEmpresaId(usuarioEmpresaDto: UsuarioEmpresaDTO): List<EventoConUsuarioDTO>{
        return eventoRepository.getEventosByUsuarioIdAndEmpresaId(
            usuarioEmpresaDto.usuarioId,
            usuarioEmpresaDto.empresaId
        )
    }

    fun getCantEventosByUsuarioIdAndEmpresaId(usuarioEmpresaDto: UsuarioEmpresaDTO): Int {
        return eventoRepository.getCantEventosByUsuarioIdAndEmpresaId(
            usuarioEmpresaDto.usuarioId,
            usuarioEmpresaDto.empresaId
        )
    }

    fun getAllEventosForAgendaByFecha(fecha: String, empresaId : Long): List<EventoDTO> {
        val fechaInicio : LocalDateTime = LocalDateTime.parse(fecha + "T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val fechaFin : LocalDateTime = LocalDateTime.parse(fecha + "T23:59:59", DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        return eventoRepository.getAllEventosForAgendaByFecha(fechaInicio, fechaFin, empresaId)
    }

    @Transactional(readOnly = true)
    fun findById(id : Long): Evento {
        return eventoRepository.findById(id).get()
    }

    fun contadorDeEventos(id : Long): Int {
        return eventoRepository.cantidadDeEventos(id)
    }

    fun contadorDeEventosFiltrados(id : Long, buscar : String): Int {
        return eventoRepository.cantidadDeEventosFiltrados(id,buscar)
    }

    fun listaEventoToListaEventoDto(listaEvento : MutableList<Evento>?) : List<EventoDTO>?{
        return listaEvento!!.map { it.toDto() }
    }

    fun generateCodigoForEventoOfEmpresa(empresa : Empresa) : String{
        var codigo : String = CodeGeneratorUtil.base26Only4Letters

        try{
            while (this.existCodigoInEmpresa(codigo, empresa)){
                codigo = CodeGeneratorUtil.base26Only4Letters
            }
        }catch (error : NullPointerException){
            return codigo
        }

        return codigo
    }

    fun existCodigoInEmpresa(codigo : String, empresa : Empresa) : Boolean{
        return empresa.listaEvento.any{ it.codigo == codigo}
    }

    fun findAllByInicioBetweenAndListaEmpresa(empresa: Empresa, desde: LocalDateTime, hasta: LocalDateTime): List<Evento> {

        // Crea la hora inicio y la hora final de un dia para buscar todos los eventos en X dia
        val inicio: LocalDateTime = LocalDateTime.of(desde.year, desde.month, desde.dayOfMonth, 0,0 )
        val fin: LocalDateTime = LocalDateTime.of(desde.year, desde.month, desde.dayOfMonth, 23,59 )

        return eventoRepository.findAllByInicioBetweenAndEmpresa(inicio, fin, empresa)
    }

    fun getHorarioDisponible(listaEvento: List<Evento>, desde : LocalDateTime, hasta : LocalDateTime) : Boolean{
        
        // En caso de no existir ningun evento para esa fecha devolver disponible
        if (listaEvento.isNotEmpty()) {

            // lista de todas las lista de rangos horarios
            val listaDeRangos: MutableList<List<Int>> = ArrayList()

            // Variable usada para obtener la hora final del evento
            var horaFinal : String

            // Obtiene el rango horario de los eventos agendados
            for (evento in listaEvento) {
                if (evento.inicio.plusDays(1).dayOfMonth == evento.fin.dayOfMonth) {
                    horaFinal = suma24Horas(evento.fin)
                } else {
                    horaFinal = evento.fin.toLocalTime().toString()
                }
                listaDeRangos.add(getRango(evento.inicio.toLocalTime().toString(), horaFinal))
            }

            // Obtiene el rango horario del nuevo evento a agendar
            if (desde.dayOfMonth != hasta.dayOfMonth) {
                horaFinal = suma24Horas(hasta)
            } else {
                horaFinal = hasta.toLocalTime().toString()
            }
            val rangoEventoNuevo = getRangoConMargen(desde.toLocalTime().toString(), horaFinal)

            // Si intercepta algun rango de hora
            for (rangos in listaDeRangos) {
                if (CollectionUtils.containsAny(rangos, rangoEventoNuevo)){
                    return false
                }
            }
        }
        return true
    }

    private fun getRango(inicio: String, fin: String): List<Int> {
        val horaInicioSplit = inicio.split(":")
        val horaFinSplit = fin.split(":")

        val horaInicio = (horaInicioSplit[0] + horaInicioSplit[1]).toInt()
        val horaFin = (horaFinSplit[0] + horaFinSplit[1]).toInt()

        return IntStream.range(horaInicio, horaFin).boxed().collect(Collectors.toList())
    }

    private fun getRangoConMargen(inicio: String, fin: String): List<Int> {
        val horaInicioSplit = inicio.split(":")
        val horaFinSplit = fin.split(":")

        var horaInicio = (horaInicioSplit[0] + horaInicioSplit[1]).toInt()
        var horaFin = (horaFinSplit[0] + horaFinSplit[1]).toInt()

        // Le agrega una hora antes y una hora despues para tener margen
        horaInicio -= 100
        horaFin += 100
        return IntStream.range(horaInicio, horaFin).boxed().collect(Collectors.toList())
    }

    private fun suma24Horas(fechaFin: LocalDateTime): String {
        var horaFin: String = fechaFin.toLocalTime().hour.toString()
        val finHoraEventos = horaFin.toInt() + 24
        horaFin = Integer.toString(finHoraEventos)

        var minutosFin: String = fechaFin.toLocalTime().minute.toString()

        if(minutosFin.length == 1){
            minutosFin = "00"
        }

        return "$horaFin:$minutosFin"
    }

    fun fromEventoReservaDtoToEvento(eventoReservaDto : EventoReservaDTO,
                                     tipoEvento : TipoEvento,
                                     listaExtra : MutableSet<Extra>,
                                     listaEventoExtraVariable : MutableSet<EventoExtraVariable>,
                                     encargado : Usuario,
                                     empresa : Empresa): Evento {
        return Evento(
            eventoReservaDto.id,
            eventoReservaDto.nombre,
            tipoEvento,
            eventoReservaDto.inicio,
            eventoReservaDto.fin,
            eventoReservaDto.capacidad,
            eventoReservaDto.extraOtro,
            eventoReservaDto.descuento,
            listaExtra,
            listaEventoExtraVariable,
            eventoReservaDto.cateringOtro,
            eventoReservaDto.cateringOtroDescripcion,
            encargado,
            eventoReservaDto.cliente,
            eventoReservaDto.codigo,
            eventoReservaDto.estado,
            eventoReservaDto.anotaciones,
            empresa)
    }

    @Transactional(readOnly = true)
    fun getByCodigoAndEmpresaId(codigo : String, empresaId : Long): Evento {
        return eventoRepository.getByCodigoAndEmpresaId(codigo, empresaId)
    }

    @Transactional
    fun registrarReserva(dto: EventoReservaDTO): Long {
        val empresa = empresaService.findById(dto.empresaId)

        // 1. Lógica delegada del Código (Limpieza del TODO)
        if (dto.codigo.isNullOrBlank()) {
            dto.codigo = generateCodigoForEventoOfEmpresa(empresa)
        }

        // 2. Reutilización de Capacidad
        val capacidad = capacidadService.reutilizarCapacidad(dto.capacidad)

        // 3. Unificación de Extras (Reducción de las listas del DTO)
        val listaExtra = mutableSetOf<Extra>().apply {
            addAll(extraService.fromListaExtraDtoToListaExtra(dto.listaExtra))
            addAll(extraService.fromListaExtraDtoToListaExtra(dto.listaExtraTipoCatering))
        }

        val listaEventoExtraVariable = mutableSetOf<EventoExtraVariable>().apply {
            addAll(extraVariableService.fromListaExtraVariableDtoToListaExtraVariable(dto.listaExtraVariable))
            addAll(extraVariableService.fromListaExtraVariableDtoToListaExtraVariable(dto.listaExtraCateringVariable))
        }

        // 4. Procesar y Normalizar al Cliente según las nuevas reglas del negocio
        val cliente = procesarYNormalizarCliente(dto.cliente)

        // 5. Construcción de la Entidad Evento
        val tipoEvento = tipoEventoService.get(dto.tipoEventoId) ?: throw IllegalArgumentException("Tipo Evento no encontrado")
        val encargado = usuarioService.findById(dto.encargadoId) ?: throw IllegalArgumentException("Encargado no encontrado")

        val evento = Evento(
            id = dto.id,
            nombre = dto.nombre.trim().lowercase(), // Lo guardamos normalizado
            tipoEvento = tipoEvento,
            inicio = dto.inicio,
            fin = dto.fin,
            capacidad = capacidad,
            extraOtro = dto.extraOtro,
            descuento = dto.descuento,
            listaExtra = listaExtra,
            listaEventoExtraVariable = mutableSetOf(), // Inicialmente vacío para asociarlo vía cascade
            cateringOtro = dto.cateringOtro,
            cateringOtroDescripcion = dto.cateringOtroDescripcion,
            encargado = encargado,
            cliente = cliente,
            codigo = dto.codigo,
            estado = dto.estado,
            anotaciones = dto.anotaciones,
            empresa = empresa
        )

        // El Secreto del Cascade: Vinculamos las variables al evento ANTES de guardar
        listaEventoExtraVariable.forEach { variable ->
            variable.evento = evento
            evento.listaEventoExtraVariable.add(variable)
        }

        // 6. Guardado unificado (Guarda Evento y sus EventoExtraVariable por cascade)
        val eventoSaved = eventoRepository.save(evento)

        // 7. Notificación asíncrona / controlada por excepciones
        enviarMailComprobanteSeguro(eventoSaved, empresa)

        return eventoSaved.id
    }

    private fun procesarYNormalizarCliente(clienteInput: Usuario): Usuario {
        // Si ya viene con ID, lo buscamos directo
        if (clienteInput.id != 0L) {
            return usuarioService.get(clienteInput.id) ?: throw IllegalArgumentException("Cliente no encontrado")
        }

        // Normalizamos los datos de contacto recibidos antes de comparar
        val emailNormalizado = clienteInput.email.trim().lowercase()
        val celularNormalizado = clienteInput.celular // Asumiendo que el frontend ya manda 10 dígitos

        // Aplicamos las reglas de limpieza que usamos en los scripts SQL para nuevos registros
        clienteInput.nombre = clienteInput.nombre.trim().lowercase()
        clienteInput.apellido = clienteInput.apellido.trim().lowercase()
        clienteInput.email = emailNormalizado

        // Clientes nuevos entran sin credenciales (NULL) tal como definimos
        clienteInput.username = null
        clienteInput.password = null

        return when {
            emailNormalizado.isNotBlank() && usuarioService.existsByEmail(emailNormalizado) -> {
                usuarioService.getByEmail(emailNormalizado)!!
            }
            usuarioService.existsByCelular(celularNormalizado) -> {
                usuarioService.getByCelular(celularNormalizado)!!
            }
            else -> {
                usuarioService.save(clienteInput)
            }
        }
    }

    private fun enviarMailComprobanteSeguro(evento: Evento, empresa: Empresa) {
        try {
            if (emailService.isEmailValid(evento.cliente.email)) {
                emailService.enviarMailComprabanteReserva(evento, "sido reservado", empresa)
            }
        } catch (e: Exception) {
            // TODO: Cambiar por tu logger del sistema (ej: log.error("...", e))
            println("Fallo al enviar el mail de comprobante para el evento ${evento.id}: ${e.message}")
        }
    }

    private fun generateCodigoForEventoOfEmpresa(empresa: Empresa): String {
        // Tu lógica actual de generación de código...
        return "COD-" + System.currentTimeMillis()
    }
}
}