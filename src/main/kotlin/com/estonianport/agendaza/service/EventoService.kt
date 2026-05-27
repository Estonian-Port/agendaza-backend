package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.common.codeGeneratorUtil.CodeGeneratorUtil
import com.estonianport.agendaza.common.emailService.EmailService
import com.estonianport.agendaza.common.openPDF.PdfService
import com.estonianport.agendaza.dto.*
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Evento
import com.estonianport.agendaza.model.EventoExtraVariable
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.TipoEvento
import com.estonianport.agendaza.model.Usuario
import com.estonianport.agendaza.model.enums.Estado
import com.estonianport.agendaza.model.enums.TipoExtra
import com.estonianport.agendaza.repository.EventoRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class EventoService(
    private val eventoRepository: EventoRepository,
    private val empresaService: EmpresaService,
    private val extraService: ExtraService,
    private val extraVariableService: ExtraVariableService,
    private val emailService: EmailService,
    private val usuarioService: UsuarioService,
    private val pdfService: PdfService,
    private val tipoEventoService: TipoEventoService,
    private val capacidadService: CapacidadService
    ) : GenericServiceImpl<Evento, Long>() {

    override val dao: CrudRepository<Evento, Long>
        get() = eventoRepository

    // ==================== VARIABLES DE ESTADO ====================

    var eventoId: Long = 0
    var fechaFiltroForAbmEvento: String = ""

    fun findById(id: Long): Evento {
        return eventoRepository.findById(id).orElseThrow {
            IllegalArgumentException("Evento no encontrado con el ID: $id")
        }
    }

    fun asignarEventoId(id: Long) {
        this.eventoId = id
    }

    fun asignarFechaFiltro(fecha: String) {
        this.fechaFiltroForAbmEvento = fecha
    }

    // ==================== OPERACIONES DE PERSISTENCIA Y NEGOCIO ====================

    @Transactional(readOnly = true)
    fun getByCodigoAndEmpresaId(codigo : String, empresaId : Long): Evento {
        return eventoRepository.getByCodigoAndEmpresaId(codigo, empresaId)
    }

    // TODO REFACTOR
    @Transactional
    @CacheEvict(value = ["eventoVer", "eventoHora", "eventoCatering", "eventoExtra", "eventoAgenda"], allEntries = true)
    fun registrarReserva(dto: EventoReservaDTO): Long {
        val empresa = empresaService.findById(dto.empresaId)

        // 1. Lógica delegada del Código (Limpieza del TODO)
        if (dto.codigo.isBlank()) {
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

    /**
     * Normalizar datos del cliente y evitar duplicados
     */
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

    /**
     * Envío seguro de comprobante envolviendo posibles fallos de infraestructura de red
     */
    private fun enviarMailComprobanteSeguro(evento: Evento, empresa: Empresa) {
        try {
            if (emailService.isEmailValid(evento.cliente.email)) {
                emailService.enviarMailComprabanteReserva(evento, "sido reservado", empresa)
            }
        } catch (e: Exception) {
            println("Fallo al enviar el mail de comprobante para el evento ${evento.id}: ${e.message}")
        }
    }

    fun generateCodigoForEventoOfEmpresa(empresa: Empresa): String {
        var codigo: String = CodeGeneratorUtil.base26Only4Letters

        while (eventoRepository.existCodigoInEmpresa(codigo, empresa)) {
            codigo = CodeGeneratorUtil.base26Only4Letters
        }

        return codigo
    }

    @Transactional(readOnly = true)
    fun getPresupuesto(eventoId: Long): Double {
        return findById(eventoId).getPresupuestoTotal()
    }

    // ==================== BÚSQUEDAS Y LISTADOS ====================

    @Transactional(readOnly = true)
    fun getEventosByUsuarioAndEmpresa(usuarioId: Long, empresaId: Long): List<EventoConUsuarioDTO> {
        return eventoRepository.getEventosByUsuarioIdAndEmpresaId(
            usuarioId,
            empresaId
        )
    }

    @Transactional(readOnly = true)
    fun getCantEventosByUsuarioAndEmpresa(usuarioId: Long, empresaId: Long): Int {
        return eventoRepository.getCantEventosByUsuarioIdAndEmpresaId(usuarioId, empresaId)
    }

    @Transactional(readOnly = true)
    fun getAllEventoByEmpresaId(empresaId: Long, pageNumber: Int): List<EventoDTO> {
        return eventoRepository.eventosByEmpresa(empresaId, PageRequest.of(pageNumber, 10)).content
    }

    @Transactional(readOnly = true)
    fun getAllEventosByFecha(empresa: Empresa): List<EventoDTO> {
        val inicio = LocalDateTime.parse(fechaFiltroForAbmEvento + "T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val fin = inicio.plusDays(1).minusSeconds(1)
        return eventoRepository.findAllByInicioBetweenAndEmpresa(inicio, fin, empresa)
    }

    @Transactional(readOnly = true)
    fun getAllEventoByFilterName(empresaId: Long, pageNumber: Int, buscar: String): List<EventoDTO> {
        return eventoRepository.eventosByNombre(empresaId, buscar, PageRequest.of(pageNumber, 10)).content
    }

    @Transactional(readOnly = true)
    fun cantEventos(empresaId: Long): Int = eventoRepository.cantidadDeEventos(empresaId)

    @Transactional(readOnly = true)
    fun cantEventosFiltrados(empresaId: Long, buscar: String): Int =
        eventoRepository.cantidadDeEventosFiltrados(empresaId, buscar)

    // TODO REFACTOR
    @Transactional(readOnly = true)
    @Cacheable(value = ["eventoVer"], key = "#eventoId")
    fun getEventoVer(eventoId: Long): EventoVerDTO? {
        val evento = findById(eventoId)
        val empresa = evento.empresa
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

    // TODO REFACTOR
    @Transactional(readOnly = true)
    @Cacheable(value = ["eventoExtra"], key = "#eventoId")
    fun getEventoExtra(eventoId: Long): EventoExtraDTO? {
        val evento = findById(eventoId)
        val empresa = evento.empresa
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

    // TODO REFACTOR
    @Transactional(readOnly = true)
    @Cacheable(value = ["eventoCatering"], key = "#eventoId")
    fun getEventoCatering(eventoId: Long): EventoCateringDTO? {
        val evento = findById(eventoId)
        val empresa = evento.empresa
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

    // TODO REFACTOR PASAR A REPOSITORY DIRECTO DTO
    @Transactional(readOnly = true)
    @Cacheable(value = ["eventoHora"], key = "#eventoId")
    fun getEventoHora(eventoId: Long): EventoHoraDTO? {
        return findById(eventoId).toEventoHoraDto()
    }

    // =================== ESTADO EVENTO ======================

    @Transactional(readOnly = true)
    fun getAllEstado(): List<String> {
        return Estado.entries.map { it.name }
    }

    @Transactional(readOnly = true)
    fun getAllEstadoForSaveEvento(): List<String> {
        return listOf(Estado.COTIZADO.name, Estado.RESERVADO.name)
    }

    // ==================== DISPONIBILIDAD ====================

    @Transactional(readOnly = true)
    fun getListaEventoByDiaAndEmpresaId(dto: EventoBuscarFechaDTO): List<String> {
        val empresa = empresaService.findById(dto.empresaId)
        val inicio = LocalDateTime.of(dto.desde.year, dto.desde.month, dto.desde.dayOfMonth, 0, 0)
        val fin = LocalDateTime.of(dto.hasta.year, dto.hasta.month, dto.hasta.dayOfMonth, 23, 59)

        val listaEvento = eventoRepository.findAllByInicioBetweenAndEmpresa(inicio, fin, empresa)

        return listaEvento.map { evento ->
            if (evento.inicio.plusDays(1).dayOfMonth == evento.fin.dayOfMonth) {
                "${evento.inicio.toLocalTime()} hasta ${evento.fin.toLocalTime()} del dia ${evento.fin.toLocalDate()}"
            } else {
                "${evento.inicio.toLocalTime()} hasta ${evento.fin.toLocalTime()}"
            }
        }
    }

    @Transactional(readOnly = true)
    fun getHorarioDisponible(dto: EventoBuscarFechaDTO): Boolean {
        val inicioConMargen = dto.desde.minusHours(1)
        val finConMargen = dto.hasta.plusHours(1)

        val haySuperposicion = eventoRepository.existeSuperposicionDeHorarios(
            dto.empresaId,
            inicioConMargen,
            finConMargen
        )
        return !haySuperposicion
    }

    // ==================== ACTUALIZAR INFORMACIÓN EDIT COORDENADAS ====================

    @Transactional
    @CacheEvict(value = ["eventoHora"], key = "#eventoHoraDto.id")
    fun editEventoHora(eventoHoraDto: EventoHoraDTO): EventoHoraDTO {
        val evento = findById(eventoHoraDto.id)
        evento.inicio = eventoHoraDto.inicio
        evento.fin = eventoHoraDto.fin
        return save(evento).toEventoHoraDto()
    }

    @Transactional
    @CacheEvict(value = ["eventoVer"], key = "#eventoVer.id")
    fun editEventoCantNinos(eventoVer: EventoVerDTO): Int {
        val evento = findById(eventoVer.id)
        evento.capacidad.capacidadNinos = eventoVer.capacidad.capacidadNinos
        save(evento)
        return evento.capacidad.capacidadNinos
    }

    @Transactional
    @CacheEvict(value = ["eventoVer"], key = "#eventoVer.id")
    fun editEventoCantAdultos(eventoVer: EventoVerDTO): Int {
        val evento = findById(eventoVer.id)
        evento.capacidad.capacidadAdultos = eventoVer.capacidad.capacidadAdultos
        save(evento)
        return evento.capacidad.capacidadAdultos
    }

    // TODO REFACTOR
    @Transactional
    @CacheEvict(value = ["eventoExtra"], key = "#evento.id")
    fun editEventoExtra(eventoDTO: EventoExtraDTO): Long {
        val evento = findById(eventoDTO.id)

        val listaExtra = mutableSetOf<Extra>()
        listaExtra.addAll(
            extraService.fromListaExtraDtoToListaExtra(
                eventoDTO.listaExtra.toList()
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
                eventoDTO.listaExtraVariable.toList()
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

        evento.extraOtro = eventoDTO.extraOtro
        evento.descuento = eventoDTO.descuento

        return save(evento).id
    }

    @Transactional
    fun editEventoAnotaciones(anotaciones: String, id: Long): String {
        val evento = findById(id)
        evento.anotaciones = anotaciones

        return save(evento).anotaciones
    }

    // TODO REFACTOR
    @Transactional
    @CacheEvict(value = ["eventoCatering"], key = "#evento.id")
    fun editEventoCatering(eventoCateringDto: EventoCateringDTO): Long {
        val evento = findById(eventoCateringDto.id)

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

        save(evento)

        return evento.id
    }

    @Transactional
    fun editEventoNombre(nombre: String, id: Long): String {
        val evento = findById(id)
        evento.nombre = nombre

        return save(evento).nombre
    }

    // TODO REFACTOR
    @Transactional
    fun recorrerEspecificaciones(eventoReservaDto: EventoReservaDTO, empresaId: Long): Any {
        val empresa = empresaService.get(empresaId)!!
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

        val encargado = usuarioService.findById(eventoReservaDto.encargadoId)!!

        val evento = fromEventoReservaDtoToEvento(
            eventoReservaDto,
            tipoEvento,
            listaExtra,
            listaEventoExtraVariable,
            encargado,
            empresa
        )

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
            )
        )
    }

    // ==================== DESCARGAS PDF ====================

    @Transactional(readOnly = true)
    fun descargarEvento(eventoId: Long): ByteArray {
        val evento = findById(eventoId)
        return pdfService.generarComprobanteEvento(evento)
    }

    @Transactional(readOnly = true)
    fun generarEstadoDeCuentaPDF(): ByteArray {
        val evento = findById(this.eventoId)
        return pdfService.generarEstadoDeCuenta(evento)
    }

    // ==================== AGENDA ====================

    @Transactional(readOnly = true)
    fun getAllEventosForAgendaByEmpresaId(empresaId: Long): List<EventoAgendaDTO> {
        val desde = LocalDateTime.now().minusMonths(2)
        return eventoRepository.getAllEventosForAgendaByEmpresaId(empresaId, desde)
    }

    // ==================== ELIMINAR (SOFT DELETE RECONSTRUIDO) ====================

    @Transactional
    @CacheEvict(value = ["eventoVer", "eventoHora", "eventoCatering", "eventoExtra", "eventoAgenda"], allEntries = true)
    override fun delete(id: Long) {
        val evento = findById(id)
        evento.fechaBaja = LocalDate.now()
        super.save(evento)
    }

    // ==================== COMUNICACIÓN ====================

    //TODO REFACTOR Transaccional que llama a api externa?
    @Transactional
    fun reenviarMail(eventoId: Long, empresaId: Long): Boolean {
        try {
            val evento = findById(eventoId)
            val empresa = empresaService.findById(empresaId)

            emailService.enviarMailComprabanteReserva(evento, "sido reservado (reenvio)", empresa)
            return true
        } catch (e: Exception) {
            throw NotFoundException("No se pudo reenviar mail")
        }
    }

    // ===================== METODOS AUXILIARES DE CONVERSION =============================

    fun fromEventoReservaDtoToEvento(
        eventoReservaDto: EventoReservaDTO,
        tipoEvento: TipoEvento,
        listaExtra: MutableSet<Extra>,
        listaEventoExtraVariable: MutableSet<EventoExtraVariable>,
        encargado: Usuario,
        empresa: Empresa
    ): Evento {
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
            empresa
        )
    }
}