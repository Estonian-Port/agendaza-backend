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
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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

    /**
     * Busca un evento por ID y lanza excepción si no existe
     */
    fun findById(id: Long): Evento {
        return eventoRepository.findById(id).orElseThrow {
            IllegalArgumentException("Evento no encontrado con el ID: $id")
        }
    }

    /**
     * Busca un evento por código dentro de una empresa
     */
    @Transactional(readOnly = true)
    fun getByCodigoAndEmpresaId(codigo: String, empresaId: Long): Evento {
        return eventoRepository.getByCodigoAndEmpresaId(codigo, empresaId)
    }

    // ==================== OPERACIONES DE PERSISTENCIA Y NEGOCIO ====================

    /**
     * Registra una nueva reserva de evento con todas sus validaciones y relaciones
     */
    @Transactional
    @CacheEvict(value = ["eventoVer", "eventoHora", "eventoCatering", "eventoExtra", "eventoAgenda"], allEntries = true)
    fun registrarReserva(dto: EventoReservaDTO): Long {
        val empresa = empresaService.findById(dto.empresaId)

        // 1. Generar código si es necesario
        val codigo = dto.codigo.ifBlank {
            generateCodigoForEventoOfEmpresa(empresa)
        }

        // 2. Reutilizar o crear capacidad
        val capacidad = capacidadService.reutilizarCapacidad(dto.capacidad)

        // 3. Unificar extras de evento y catering
        val listaExtra = mutableSetOf<Extra>().apply {
            addAll(extraService.fromListaExtraDtoToListaExtra(dto.listaExtra))
            addAll(extraService.fromListaExtraDtoToListaExtra(dto.listaExtraTipoCatering))
        }

        // 4. Unificar extras variables
        val listaEventoExtraVariable = mutableSetOf<EventoExtraVariable>().apply {
            addAll(extraVariableService.fromListaExtraVariableDtoToListaExtraVariable(dto.listaExtraVariable))
            addAll(extraVariableService.fromListaExtraVariableDtoToListaExtraVariable(dto.listaExtraCateringVariable))
        }

        // 5. Procesar cliente
        val cliente = procesarYNormalizarCliente(dto.cliente)

        // 6. Obtener referencias necesarias
        val tipoEvento = tipoEventoService.get(dto.tipoEventoId)
            ?: throw IllegalArgumentException("Tipo Evento no encontrado")
        val encargado = usuarioService.findById(dto.encargadoId)
            ?: throw IllegalArgumentException("Encargado no encontrado")

        // 7. Crear entidad evento
        val evento = Evento(
            id = dto.id,
            nombre = dto.nombre.trim().lowercase(),
            tipoEvento = tipoEvento,
            inicio = dto.inicio,
            fin = dto.fin,
            capacidad = capacidad,
            extraOtro = dto.extraOtro,
            descuento = dto.descuento,
            listaExtra = listaExtra,
            listaEventoExtraVariable = mutableSetOf(),
            cateringOtro = dto.cateringOtro,
            cateringOtroDescripcion = dto.cateringOtroDescripcion,
            encargado = encargado,
            cliente = cliente,
            codigo = codigo,
            estado = dto.estado,
            anotaciones = dto.anotaciones,
            empresa = empresa
        )

        // 8. Vincular extras variables al evento (cascade)
        listaEventoExtraVariable.forEach { variable ->
            variable.evento = evento
            evento.listaEventoExtraVariable.add(variable)
        }

        // 9. Guardar evento
        val eventoSaved = eventoRepository.save(evento)

        // 10. Notificar cliente
        enviarMailComprobanteSeguro(eventoSaved, empresa)

        return eventoSaved.id
    }

    /**
     * Procesa y normaliza datos del cliente, evitando duplicados
     */
    private fun procesarYNormalizarCliente(clienteInput: Usuario): Usuario {
        // Si viene con ID, buscarlo directamente
        if (clienteInput.id != 0L) {
            return usuarioService.get(clienteInput.id)
                ?: throw IllegalArgumentException("Cliente no encontrado")
        }

        // Normalizar datos
        val emailNormalizado = clienteInput.email.trim().lowercase()
        val celularNormalizado = clienteInput.celular

        clienteInput.apply {
            nombre = nombre.trim().lowercase()
            apellido = apellido.trim().lowercase()
            email = emailNormalizado
            username = null
            password = null
        }

        // Buscar cliente existente
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
     * Envía email de comprobante de forma segura, sin romper la transacción
     */
    private fun enviarMailComprobanteSeguro(evento: Evento, empresa: Empresa) {
        try {
            if (emailService.isEmailValid(evento.cliente.email)) {
                emailService.enviarMailComprabanteReserva(evento, "sido reservado", empresa)
            }
        } catch (e: Exception) {
            // Log del error sin romper la transacción
            println("Fallo al enviar el mail de comprobante para el evento ${evento.id}: ${e.message}")
        }
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
        val empresa = evento.empresa

        return evento.toEventoVerDto(
            extraService.fromListaExtraToListaExtraDtoByFilter(
                empresa, evento.listaExtra, evento.inicio, TipoExtra.EVENTO
            ),
            extraVariableService.fromListaExtraVariableToListaExtraVariableDtoByFilter(
                empresa, evento.listaEventoExtraVariable, evento.inicio, TipoExtra.VARIABLE_EVENTO
            ),
            extraService.fromListaExtraToListaExtraDtoByFilter(
                empresa, evento.listaExtra, evento.inicio, TipoExtra.TIPO_CATERING
            ),
            extraVariableService.fromListaExtraVariableToListaExtraVariableDtoByFilter(
                empresa, evento.listaEventoExtraVariable, evento.inicio, TipoExtra.VARIABLE_CATERING
            )
        )
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["eventoExtra"], key = "#eventoId")
    fun getEventoExtra(eventoId: Long): EventoExtraDTO? {

        val evento = findById(eventoId)
        val empresa = evento.empresa

        return evento.toEventoExtraDto(
            extraService.fromListaExtraToListaExtraDtoByFilter(
                empresa, evento.listaExtra, evento.inicio, TipoExtra.EVENTO
            ),
            extraVariableService.fromListaExtraVariableToListaExtraVariableDtoByFilter(
                empresa, evento.listaEventoExtraVariable, evento.inicio, TipoExtra.VARIABLE_EVENTO
            )
        )
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["eventoCatering"], key = "#eventoId")
    fun getEventoCatering(eventoId: Long): EventoCateringDTO? {
        val evento = findById(eventoId)
        val empresa = evento.empresa

        return evento.toEventoCateringDto(
            extraService.fromListaExtraToListaExtraDtoByFilter(
                empresa, evento.listaExtra, evento.inicio, TipoExtra.TIPO_CATERING
            ),
            extraVariableService.fromListaExtraVariableToListaExtraVariableDtoByFilter(
                empresa, evento.listaEventoExtraVariable, evento.inicio, TipoExtra.VARIABLE_CATERING
            )
        )

    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["eventoHora"], key = "#eventoId")
    fun getEventoHora(eventoId: Long): EventoHoraDTO? = findById(eventoId).toEventoHoraDto()


    // ==================== EDICIONES PARCIALES ====================

    @Transactional
    @CacheEvict(value = ["eventoHora"], key = "#evento.id")
    fun editEventoHora(evento: EventoHoraDTO): EventoHoraDTO {
        val eventoExistente = findById(evento.id)
        eventoExistente.inicio = evento.inicio
        eventoExistente.fin = evento.fin

        val saved = save(eventoExistente)
        return saved.toEventoHoraDto()
    }

    @Transactional
    @CacheEvict(value = ["eventoExtra"], key = "#eventoDTO.id")
    fun editEventoExtra(eventoDTO: EventoExtraDTO): Long {
        val evento = findById(eventoDTO.id)

        // 1. Convertimos los DTOs que vienen del front a entidades
        val nuevosExtras = extraService.fromListaExtraDtoToListaExtra(eventoDTO.listaExtra)
        val nuevasVariables = extraVariableService.fromListaExtraVariableDtoToListaExtraVariable(eventoDTO.listaExtraVariable)

        // 2. ACTUALIZAR EXTRAS NORMALES
        evento.listaExtra.removeAll { it.tipoExtra != TipoExtra.TIPO_CATERING }

        // Agregamos los nuevos que vinieron del front
        evento.listaExtra.addAll(nuevosExtras)

        // 3. ACTUALIZAR EXTRAS VARIABLES
        evento.listaEventoExtraVariable.removeAll { it.extra.tipoExtra == TipoExtra.VARIABLE_EVENTO }

        // Le asignamos el evento padre a las nuevas variables y las agregamos a la colección
        nuevasVariables.forEach { it.evento = evento }
        evento.listaEventoExtraVariable.addAll(nuevasVariables)

        // 4. Actualizar campos básicos
        evento.extraOtro = eventoDTO.extraOtro
        evento.descuento = eventoDTO.descuento

        // 5. Guardar
        return save(evento).id
    }

    @Transactional
    @CacheEvict(value = ["eventoCatering"], key = "#eventoCateringDto.id")
    fun editEventoCatering(eventoCateringDto: EventoCateringDTO): Long {
        val evento = findById(eventoCateringDto.id)

        // 1. Convertimos los DTOs que vienen del front a entidades
        val nuevosExtrasCatering = extraService.fromListaExtraDtoToListaExtra(eventoCateringDto.listaExtraTipoCatering.toList())
        val nuevasVariablesCatering = extraVariableService.fromListaExtraVariableDtoToListaExtraVariable(eventoCateringDto.listaExtraCateringVariable.toList())

        // 2. ACTUALIZAR EXTRAS NORMALES
        evento.listaExtra.removeAll { it.tipoExtra != TipoExtra.EVENTO }

        // Agregamos los nuevos de catering
        evento.listaExtra.addAll(nuevosExtrasCatering)

        // 3. ACTUALIZAR EXTRAS VARIABLES
        evento.listaEventoExtraVariable.removeAll { it.extra.tipoExtra == TipoExtra.VARIABLE_CATERING }

        // Le asignamos el evento padre a las nuevas variables y las agregamos
        nuevasVariablesCatering.forEach { it.evento = evento }
        evento.listaEventoExtraVariable.addAll(nuevasVariablesCatering)

        // 4. Actualizar campos básicos
        evento.cateringOtro = eventoCateringDto.cateringOtro
        evento.cateringOtroDescripcion = eventoCateringDto.cateringOtroDescripcion

        // 5. Guardar
        return save(evento).id
    }

    @Transactional
    fun editEventoAnotaciones(anotaciones: String, eventoId: Long): String {
        val evento = findById(eventoId)
        evento.anotaciones = anotaciones

        return save(evento).anotaciones
    }

    @Transactional
    fun editEventoCapacidad(eventoId: Long, capacidad: EventoCapacidadDTO): EventoCapacidadDTO {
        val evento = findById(eventoId)
        evento.capacidad.capacidadAdultos = capacidad.capacidadAdultos
        evento.capacidad.capacidadNinos = capacidad.capacidadNinos

        val saved = save(evento)
        return EventoCapacidadDTO(
            capacidadAdultos = saved.capacidad.capacidadAdultos,
            capacidadNinos = saved.capacidad.capacidadNinos
        )
    }

    @Transactional
    fun editEventoNombre(nombre: String, eventoId: Long): String {
        val evento = findById(eventoId)
        evento.nombre = nombre

        return save(evento).nombre
    }

    // ==================== PROCESAMIENTO ESPECIAL ====================

    @Transactional
    fun recorrerEspecificaciones(eventoReservaDto: EventoReservaDTO, empresaId: Long): EventoReservaDTO {
        val empresa = empresaService.get(empresaId)
            ?: throw IllegalArgumentException("Empresa no encontrada")
        val tipoEvento = tipoEventoService.get(eventoReservaDto.tipoEventoId)
            ?: throw IllegalArgumentException("Tipo evento no encontrado")

        val listaExtra = mutableSetOf<Extra>()
        listaExtra.addAll(
            extraService.fromListaExtraDtoToListaExtra(eventoReservaDto.listaExtraTipoCatering.toList())
        )

        val listaEventoExtraVariable = mutableSetOf<EventoExtraVariable>()
        listaEventoExtraVariable.addAll(
            extraVariableService.fromListaExtraVariableDtoToListaExtraVariable(
                eventoReservaDto.listaExtraCateringVariable.toList()
            )
        )

        val encargado = usuarioService.findById(eventoReservaDto.encargadoId)
            ?: throw IllegalArgumentException("Encargado no encontrado")

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
                empresa, evento.listaExtra, evento.inicio, TipoExtra.EVENTO
            ),
            extraVariableService.fromListaExtraVariableToListaExtraVariableDtoByFilter(
                empresa, evento.listaEventoExtraVariable, evento.inicio, TipoExtra.VARIABLE_EVENTO
            ),
            extraService.fromListaExtraToListaExtraDtoByFilter(
                empresa, evento.listaExtra, evento.inicio, TipoExtra.TIPO_CATERING
            ),
            extraVariableService.fromListaExtraVariableToListaExtraVariableDtoByFilter(
                empresa, evento.listaEventoExtraVariable, evento.inicio, TipoExtra.VARIABLE_CATERING
            )
        )
    }

    // ==================== VALIDACIONES ====================

    /**
     * Obtiene horarios disponibles para un período en una empresa
     * @param eventoBuscarFecha DTO con empresa, desde, hasta
     */
    @Transactional(readOnly = true)
    fun getHorariosDisponibles(eventoBuscarFecha: EventoBuscarFechaDTO): List<String> {
        val eventos = eventoRepository.findAllByInicioBetweenAndEmpresa(
            eventoBuscarFecha.desde,
            eventoBuscarFecha.hasta,
            empresaService.findById(eventoBuscarFecha.empresaId)
        )

        //TODO
        // Implementar lógica de horarios disponibles
        // Esta es la lógica que se encontraba en el viejo servicio
        return emptyList() // Reemplazar con implementación real
    }

    /**
     * Valida si un horario específico está disponible para una empresa
     * @param eventoBuscarFecha DTO con empresa, desde, hasta
     */
    @Transactional(readOnly = true)
    fun getHorarioDisponible(eventoBuscarFecha: EventoBuscarFechaDTO): Boolean {
        return eventoRepository.existeSuperposicionDeHorarios(
            eventoBuscarFecha.empresaId,
            eventoBuscarFecha.desde,
            eventoBuscarFecha.hasta
        ).not()
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
        val desde = LocalDateTime.now().minusMonths(2)
        return eventoRepository.getAllEventosForAgendaByEmpresaId(empresaId, desde)
    }

    // ==================== ELIMINAR ====================

    @Transactional
    @CacheEvict(value = ["eventoVer", "eventoHora", "eventoCatering", "eventoExtra", "eventoAgenda"], allEntries = true)
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

    //TODO
    fun getAllEstadoForSaveEvento(): List<String> {
        // Filtrar si hay ciertos estados no permitidos para nuevos eventos
        return Estado.entries.map { it.name }
    }

    // ===================== MÉTODOS AUXILIARES DE CONVERSIÓN =============================

    private fun fromEventoReservaDtoToEvento(
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