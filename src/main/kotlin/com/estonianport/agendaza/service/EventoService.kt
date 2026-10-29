package com.estonianport.agendaza.service

import ReservaRegistradaEvent
import com.estonianport.agendaza.common.GenericServiceImpl
import com.estonianport.agendaza.common.codeGeneratorUtil.CodeGeneratorUtil
import com.estonianport.agendaza.common.emailService.EmailService
import com.estonianport.agendaza.common.openPDF.PdfService
import com.estonianport.agendaza.dto.*
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Evento
import com.estonianport.agendaza.model.EventoExtra
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.TipoEvento
import com.estonianport.agendaza.model.Usuario
import com.estonianport.agendaza.model.enums.Estado
import com.estonianport.agendaza.model.enums.TipoExtra
import com.estonianport.agendaza.repository.EventoRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.ApplicationEventPublisher
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class EventoService(
    private val eventoRepository: EventoRepository,
    private val empresaService: EmpresaService,
    private val extraService: ExtraService,
    private val usuarioService: UsuarioService,
    private val pdfService: PdfService,
    private val tipoEventoService: TipoEventoService,
    private val emailService: EmailService,
    private val eventPublisher: ApplicationEventPublisher
) : GenericServiceImpl<Evento, Long>() {

    override val dao: CrudRepository<Evento, Long>
        get() = eventoRepository

    /**
     * Busca un evento por ID y lanza excepción si no existe
     */
    fun findById(id: Long): Evento {
        return eventoRepository.findById(id).orElseThrow {
            NotFoundException("Evento no encontrado")
        }
    }

    /**
     * Busca un evento por código dentro de una empresa
     */
    @Transactional(readOnly = true)
    fun getByCodigoAndEmpresaId(codigo: String, empresaId: Long): Evento {
        return eventoRepository.getByCodigoAndEmpresaId(codigo, empresaId)
            ?: throw NotFoundException("Evento no encontrado con el código: $codigo")
    }

    // ==================== OPERACIONES DE PERSISTENCIA Y NEGOCIO ====================

    /**
     * Registra una nueva reserva de evento con todas sus validaciones y relaciones
     */
    @Transactional
    @CacheEvict(
        value = ["eventoVer", "eventoHora", "eventoCatering", "eventoExtra", "agendaEventos"],
        key = "#dto.empresaId"
    )
    fun registrarReserva(dto: EventoReservaDTO): EventoHoraDTO {
        val empresa = empresaService.findById(dto.empresaId)

        // 1. Generar código si es necesario
        val codigo = dto.codigo.ifBlank {
            generateCodigoForEventoOfEmpresa(empresa)
        }

        // 4. Procesar cliente
        val cliente = procesarYNormalizarCliente(dto.cliente)

        // 5. Obtener referencias necesarias
        val tipoEvento = tipoEventoService.findById(dto.tipoEventoId)
        val encargado = usuarioService.findById(dto.encargadoId)

        // 6. Crear entidad evento
        val evento = Evento(
            id = dto.id,
            nombre = dto.nombre.trim().lowercase(),
            tipoEvento = tipoEvento,
            inicio = dto.inicio,
            fin = dto.fin,
            capacidadAdultos = dto.capacidadAdultos,
            capacidadNinos = dto.capacidadNinos,
            extraOtro = dto.extraOtro,
            extraOtroDescripcion = dto.extraOtroDescripcion,
            precioTipoEvento = dto.precioTipoEvento,
            listaEventoExtra = mutableSetOf(),
            cateringOtro = dto.cateringOtro,
            cateringOtroDescripcion = dto.cateringOtroDescripcion,
            encargado = encargado,
            cliente = cliente,
            codigo = codigo,
            estado = dto.estado,
            anotaciones = dto.anotaciones,
            empresa = empresa
        )

        evento.listaEventoExtra.addAll(extraService.fromListaExtraDtoToListaExtra(dto.listaExtra, evento))

        // 9. Guardar evento
        val eventoSaved = eventoRepository.save(evento)

        // 10. Notificar cliente
        eventPublisher.publishEvent(ReservaRegistradaEvent(eventoSaved.id, empresa.id))

        return eventoSaved.toEventoHoraDto()
    }

    private fun procesarYNormalizarCliente(clienteInput: Usuario): Usuario {
        if (clienteInput.id != 0L) {
            return usuarioService.get(clienteInput.id)
                ?: throw NotFoundException("Cliente no encontrado")
        }

        val emailLimpio = clienteInput.email.trim().lowercase()
        val celularInput = clienteInput.celular

        if (emailLimpio.isNotBlank()) {
            usuarioService.getByEmail(emailLimpio)?.let { return it }
        }
        if (celularInput > 0) {
            usuarioService.getByCelular(celularInput)?.let { return it }
        }

        val emailParaGuardar = emailLimpio.ifBlank {
            "sin-email-${UUID.randomUUID()}@agendaza.com"
        }

        val celularParaGuardar = if (celularInput > 0) {
            celularInput
        } else {
            generarCelularFantasmaUnico()
        }

        val nombreParaGuardar = clienteInput.nombre.trim().lowercase().ifBlank { "cliente" }
        val apellidoParaGuardar = clienteInput.apellido.trim().lowercase().ifBlank { "cliente" }

        clienteInput.apply {
            nombre = nombreParaGuardar
            apellido = apellidoParaGuardar
            email = emailParaGuardar
            celular = celularParaGuardar
            username = null
            password = null
        }

        return try {
            usuarioService.save(clienteInput)
        } catch (ex: DataIntegrityViolationException) {
            val existente = (if (emailLimpio.isNotBlank()) usuarioService.getByEmail(emailLimpio) else null)
                ?: (if (celularInput > 0) usuarioService.getByCelular(celularInput) else null)

            existente ?: throw ex
        }
    }

    /**
     * Genera un número fantasma 999XXXXXXX y válida contra la BD que no exista antes de devolverlo.
     */
    private fun generarCelularFantasmaUnico(): Long {
        var candidato: Long
        do {
            candidato = "999${System.currentTimeMillis().toString().takeLast(7)}".toLong()
        } while (usuarioService.getByCelular(candidato) != null)

        return candidato
    }

    /**
     * Genera un código único para un evento en una empresa
     */
    private fun generateCodigoForEventoOfEmpresa(empresa: Empresa): String {
        var codigo: String = CodeGeneratorUtil.base26Only4Letters

        while (eventoRepository.existCodigoInEmpresa(codigo, empresa)) {
            codigo = CodeGeneratorUtil.base26Only4Letters
        }

        return codigo
    }

    /**
     * Obtiene el presupuesto total de un evento
     */
    @Transactional(readOnly = true)
    fun getPresupuesto(eventoId: Long): Double {
        return findById(eventoId).getPresupuestoTotal()
    }

    // ==================== BÚSQUEDAS Y LISTADOS ====================

    /**
     * Obtiene eventos de una empresa con paginación
     * @param empresaId ID de la empresa
     * @param pageable Configuración de paginación y ordenamiento
     */
    @Transactional(readOnly = true)
    fun getAllEventoByEmpresaId(empresaId: Long, pageable: Pageable): Page<EventoDTO> {
        return eventoRepository.eventosByEmpresa(empresaId, pageable)
    }

    /**
     * Obtiene eventos de una empresa filtrados por búsqueda con paginación
     * @param empresaId ID de la empresa
     * @param buscar Término de búsqueda (nombre o código)
     * @param pageable Configuración de paginación
     */
    @Transactional(readOnly = true)
    fun getAllEventoByFilterName(empresaId: Long, buscar: String, pageable: Pageable): Page<EventoDTO> {
        return eventoRepository.eventosByNombre(empresaId, buscar, pageable)
    }

    /**
     * Obtiene la cantidad total de eventos activos de una empresa
     */
    @Transactional(readOnly = true)
    fun cantEventos(empresaId: Long): Int {
        return eventoRepository.cantidadDeEventos(empresaId)
    }

    /**
     * Obtiene la cantidad de eventos filtrados por búsqueda
     */
    @Transactional(readOnly = true)
    fun cantEventosFiltrados(empresaId: Long, buscar: String): Int {
        return eventoRepository.cantidadDeEventosFiltrados(empresaId, buscar)
    }

    /**
     * Obtiene eventos de una empresa en una fecha específica
     * @param fecha Fecha en formato ISO (YYYY-MM-DD)
     * @param empresaId ID de la empresa
     */
    @Transactional(readOnly = true)
    fun getAllEventosByFecha(fecha: String, empresaId: Long): List<EventoDTO> {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val localDate = LocalDate.parse(fecha, formatter)

        val fechaInicio = LocalDateTime.of(localDate.year, localDate.month, localDate.dayOfMonth, 0, 0, 0)
        val fechaFin = LocalDateTime.of(localDate.year, localDate.month, localDate.dayOfMonth, 23, 59, 59)

        return eventoRepository.getAllEventosForAgendaByFecha(fechaInicio, fechaFin, empresaId)
    }

    /**
     * Obtiene eventos de un usuario/cliente en una empresa
     */
    @Transactional(readOnly = true)
    fun getEventosByUsuarioAndEmpresa(usuarioId: Long, empresaId: Long): List<EventoConUsuarioDTO> {
        return eventoRepository.getEventosByUsuarioIdAndEmpresaId(usuarioId, empresaId)
    }

    /**
     * Obtiene la cantidad de eventos de un usuario en una empresa
     */
    @Transactional(readOnly = true)
    fun getCantEventosByUsuarioAndEmpresa(usuarioId: Long, empresaId: Long): Int {
        return eventoRepository.getCantEventosByUsuarioIdAndEmpresaId(usuarioId, empresaId)
    }

    // ==================== OBTENER INFORMACIÓN ESPECÍFICA ====================

    @Transactional(readOnly = true)
    @Cacheable(value = ["eventoVer"], key = "#eventoId")
    fun getEventoVer(eventoId: Long): EventoVerDTO? {
        val evento = findById(eventoId)
        return evento.toEventoVerDto(extraService.aplicarPreciosFoto(evento.listaEventoExtra, TipoExtra.entries.toSet()))
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["eventoExtra"], key = "#eventoId")
    fun getEventoExtra(eventoId: Long): EventoExtraDTO? {

        val evento = findById(eventoId)
        return evento.toEventoExtraDto(
            extraService.aplicarPreciosFoto(evento.listaEventoExtra, setOf(TipoExtra.EVENTO, TipoExtra.VARIABLE_EVENTO))
        )
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["eventoCatering"], key = "#eventoId")
    fun getEventoCatering(eventoId: Long): EventoCateringDTO? {
        val evento = findById(eventoId)
        return evento.toEventoCateringDto(
            extraService.aplicarPreciosFoto(evento.listaEventoExtra, setOf(TipoExtra.TIPO_CATERING, TipoExtra.VARIABLE_CATERING))

        )

    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["eventoHora"], key = "#eventoId")
    fun getEventoHora(eventoId: Long): EventoHoraDTO? = findById(eventoId).toEventoHoraDto()


    // ==================== EDICIONES PARCIALES ====================

    @Transactional
    @CacheEvict(value = ["eventoHora", "eventoVer", "eventoAgenda"], key = "#evento.id")
    fun editEventoHora(evento: EventoHoraDTO): EventoHoraDTO {
        val eventoExistente = findById(evento.id)
        eventoExistente.inicio = evento.inicio
        eventoExistente.fin = evento.fin

        val saved = save(eventoExistente)
        return saved.toEventoHoraDto()
    }

    @Transactional
    @CacheEvict(value = ["eventoExtra", "eventoVer"], key = "#eventoDTO.id")
    fun editEventoExtra(eventoDTO: EventoExtraDTO): Long {
        val evento = findById(eventoDTO.id)

        evento.precioTipoEvento = eventoDTO.tipoEventoExtra.precio

        // 1. Convertimos los DTO que vienen del front a entidades
        val nuevosExtras = extraService.fromListaExtraDtoToListaExtra(eventoDTO.listaEventoExtra, evento)

        // 2. ACTUALIZAR EXTRAS NORMALES
        evento.listaEventoExtra.removeAll { it.extra.tipoExtra == TipoExtra.EVENTO || it.extra.tipoExtra == TipoExtra.VARIABLE_EVENTO }

        // Agregamos los nuevos que vinieron del front
        evento.listaEventoExtra.addAll(nuevosExtras)

        // 4. Actualizar campos básicos
        evento.extraOtro = eventoDTO.extraOtro
        evento.extraOtroDescripcion = eventoDTO.extraOtroDescripcion

        // 5. Guardar
        return save(evento).id
    }

    @Transactional
    @CacheEvict(value = ["eventoCatering", "eventoVer"], key = "#eventoCateringDto.id")
    fun editEventoCatering(eventoCateringDto: EventoCateringDTO): Long {
        val evento = findById(eventoCateringDto.id)

        // 1. Convertimos los DTO que vienen del front a entidades
        val nuevosExtrasCatering = extraService.fromListaExtraDtoToListaExtra(eventoCateringDto.listaExtra, evento)

        // 2. ACTUALIZAR EXTRAS NORMALES
        evento.listaEventoExtra.removeAll { it.extra.tipoExtra == TipoExtra.TIPO_CATERING || it.extra.tipoExtra == TipoExtra.VARIABLE_CATERING }

        // Agregamos los nuevos de catering
        evento.listaEventoExtra.addAll(nuevosExtrasCatering)

        // 4. Actualizar campos básicos
        evento.cateringOtro = eventoCateringDto.cateringOtro
        evento.cateringOtroDescripcion = eventoCateringDto.cateringOtroDescripcion
        evento.capacidadAdultos = eventoCateringDto.capacidadAdultos

        // 5. Guardar
        return save(evento).id
    }

    @Transactional
    @CacheEvict(value = ["eventoVer", "eventoAgenda"], key = "#eventoId")
    fun editEventoCapacidad(eventoId: Long, capacidad: EventoCapacidadDTO): EventoCapacidadDTO {
        val evento = findById(eventoId)
        evento.capacidadAdultos = capacidad.capacidadAdultos
        evento.capacidadNinos = capacidad.capacidadNinos

        val saved = save(evento)
        return EventoCapacidadDTO(
            capacidadAdultos = saved.capacidadAdultos,
            capacidadNinos = saved.capacidadNinos
        )
    }

    @Transactional
    @CacheEvict(value = ["eventoVer", "eventoExtra", "eventoCatering"], key = "#eventoId")
    fun editPreciosEvento(eventoId: Long, precios: EventoPreciosDTO): Long {
        val evento = findById(eventoId)
        evento.precioTipoEvento = precios.precioTipoEvento
        evento.capacidadAdultos = precios.capacidadAdultos
        val incoming = precios.listaExtra + precios.listaExtraVariable.map { variable ->
            val extra = extraService.get(variable.id) ?: throw NotFoundException("Extra no encontrado")
            ExtraDTO(variable.id, variable.nombre, extra.tipoExtra).also {
                it.precio = variable.precio
                it.cantidad = variable.cantidad
            }
        }
        val nuevoDetalle = extraService.fromListaExtraDtoToListaExtra(incoming, evento)
        evento.listaEventoExtra.removeAll { it.extra.tipoExtra == TipoExtra.EVENTO || it.extra.tipoExtra == TipoExtra.VARIABLE_EVENTO }
        evento.listaEventoExtra.addAll(nuevoDetalle)
        return save(evento).id
    }

    @Transactional
    @CacheEvict(value = ["eventoVer", "eventoAgenda"], key = "#eventoId")
    fun editEventoNombre(nombre: String, eventoId: Long): String {
        val evento = findById(eventoId)
        evento.nombre = nombre

        return save(evento).nombre
    }

    @Transactional
    @CacheEvict(value = ["eventoVer"], key = "#eventoId")
    fun editEventoAnotaciones(anotaciones: String, eventoId: Long): String {
        val evento = findById(eventoId)
        evento.anotaciones = anotaciones

        return save(evento).anotaciones
    }
    // ==================== PROCESAMIENTO ESPECIAL ====================

    @Transactional
    fun recorrerEspecificaciones(eventoReservaDto: EventoReservaDTO, empresaId: Long): EventoReservaDTO {
        val empresa = empresaService.get(empresaId)
            ?: throw NotFoundException("Empresa no encontrada")
        val tipoEvento = tipoEventoService.get(eventoReservaDto.tipoEventoId)
            ?: throw NotFoundException("Tipo evento no encontrado")

        val encargado = usuarioService.findById(eventoReservaDto.encargadoId)
        val evento = fromEventoReservaDtoToEvento(
            eventoReservaDto,
            tipoEvento,
            mutableSetOf(),
            encargado,
            empresa
        )
        evento.listaEventoExtra.addAll(extraService.fromListaExtraDtoToListaExtra(eventoReservaDto.listaExtra, evento))

        evento.precioTipoEvento = empresa.getPrecioOfTipoEvento(tipoEvento.id, evento.inicio)

        empresa.recorrerEspecificaciones(evento)
        evento.listaEventoExtra.forEach { item ->
            item.precio = empresa.getPrecioOfExtraByFecha(item.extra, evento.inicio)
        }

        return evento.toEventoReservaDto(extraService.aplicarPreciosFoto(evento.listaEventoExtra, TipoExtra.entries.toSet()))
    }

    // ==================== VALIDACIONES ====================

    /**
     * Obtiene una lista de strings con los horarios de los eventos ya reservados para un día.
     */
    @Transactional(readOnly = true)
    fun getEventosOcupadosDelDia(empresaId: Long, fechaEvento: LocalDateTime): List<String> {

        // 1. Definimos los límites del día
        val inicioDia = fechaEvento.toLocalDate().atStartOfDay()
        val finDia = fechaEvento.toLocalDate().atTime(LocalTime.MAX)

        // 2. Buscamos los eventos de ese día usando tu Repo (que ya devuelve EventoDTO)
        val eventosDelDia = eventoRepository.findAllByInicioBetweenAndEmpresa(
            inicioDia,
            finDia,
            empresaId
        ).sortedBy { it.inicio } // Ordenamos cronológicamente de forma segura

        // 3. Mapeamos directamente a la lista de Strings con interpolación
        return eventosDelDia.map { evento ->
            // Chequeamos si el evento termina en un día distinto al que empezó
            if (!evento.inicio.toLocalDate().isEqual(evento.fin.toLocalDate())) {
                "${evento.inicio.toLocalTime()} hasta ${evento.fin.toLocalTime()} del dia ${evento.fin.toLocalDate()} (${evento.nombre})"
            } else {
                "${evento.inicio.toLocalTime()} hasta ${evento.fin.toLocalTime()} (${evento.nombre})"
            }
        }
    }

    /**
     * Válida si un horario específico está disponible para una empresa
     */
    @Transactional(readOnly = true)
    fun getHorarioDisponible(empresaId: Long, desde: LocalDateTime, hasta: LocalDateTime): Boolean {
        return !eventoRepository.existeSuperposicionDeHorarios(empresaId, desde, hasta)
    }

    // ==================== DESCARGAS PDF ====================

    @Transactional(readOnly = true)
    fun descargarEvento(eventoId: Long): ByteArray {
        val evento = findById(eventoId)
        return pdfService.generarComprobanteEvento(evento)
    }

    @Transactional(readOnly = true)
    fun generarEstadoDeCuentaPDF(eventoId: Long): ByteArray {
        val evento = findById(eventoId)
        return pdfService.generarEstadoDeCuenta(evento)
    }

    // ==================== AGENDA ====================

    @Transactional(readOnly = true)
    @Cacheable(value = ["agendaEventos"], key = "#empresaId")
    fun getAllEventosForAgendaByEmpresaId(empresaId: Long): List<EventoAgendaDTO> {
        val desde = LocalDateTime.now().minusMonths(12)
        return eventoRepository.getAllEventosForAgendaByEmpresaId(empresaId, desde)
    }

    // ==================== ELIMINAR ====================

    @Transactional
    @CacheEvict(value = ["eventoVer", "eventoHora", "eventoCatering", "eventoExtra", "agendaEventos"])
    override fun delete(id: Long) {
        val evento = findById(id)
        evento.fechaBaja = LocalDate.now()
        super.save(evento)
    }

    // ==================== COMUNICACIÓN ====================

    @Transactional
    fun reenviarMail(eventoId: Long, empresaId: Long): Boolean {
        return try {
            val evento = findById(eventoId)
            val empresa = empresaService.findById(empresaId)

            emailService.enviarMailComprabanteReserva(evento, "sido reservado (reenvío)", empresa)
            true
        } catch (e: Exception) {
            throw NotFoundException("No se pudo reenviar mail: ${e.message}")
        }
    }

    // ==================== MÉTODOS DE OBTENCIÓN DE ENUMERADOS ====================

    fun getAllEstado(): List<String> {
        return Estado.entries.map { it.name }
    }

    fun getAllEstadoForSaveEvento(): List<String> {
        return mutableListOf(Estado.COTIZADO.name, Estado.RESERVADO.name)
    }

    // ===================== MÉTODOS AUXILIARES DE CONVERSIÓN =============================

    private fun fromEventoReservaDtoToEvento(
        eventoReservaDto: EventoReservaDTO,
        tipoEvento: TipoEvento,
        listaExtra: MutableSet<EventoExtra>,
        encargado: Usuario,
        empresa: Empresa
    ): Evento {
        return Evento(
            eventoReservaDto.id,
            eventoReservaDto.codigo,
            eventoReservaDto.nombre,
            eventoReservaDto.inicio,
            eventoReservaDto.fin,
            eventoReservaDto.capacidadAdultos,
            eventoReservaDto.capacidadNinos,
            tipoEvento,
            eventoReservaDto.precioTipoEvento,
            eventoReservaDto.extraOtro,
            eventoReservaDto.extraOtroDescripcion,
            eventoReservaDto.cateringOtro,
            eventoReservaDto.cateringOtroDescripcion,
            listaExtra,
            encargado,
            eventoReservaDto.cliente,
            eventoReservaDto.estado,
            eventoReservaDto.anotaciones,
            empresa
        )
    }
}
