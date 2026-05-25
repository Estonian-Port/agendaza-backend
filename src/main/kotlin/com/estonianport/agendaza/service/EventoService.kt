package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dto.*
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Evento
import com.estonianport.agendaza.repository.EventoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class EventoService : GenericServiceImpl<Evento, Long>() {

    @Autowired
    lateinit var eventoRepository: EventoRepository

    override val dao: CrudRepository<Evento, Long>
        get() = eventoRepository

    // ==================== VARIABLES DE ESTADO ====================

    var eventoId: Long = 0
    var fechaFiltroForAbmEvento: String = ""

    fun findById(id: Long): Evento {
        return eventoRepository.findById(id).orElseThrow {
            IllegalArgumentException("Evento no encontrada con el ID: $id")
        }
    }

    // Se cambió el nombre para evitar el choque de firmas (Platform declaration clash)
    fun asignarEventoId(id: Long) {
        this.eventoId = id
    }

    // Se cambió el nombre también aquí para prevenir el mismo error con la fecha
    fun asignarFechaFiltro(fecha: String) {
        this.fechaFiltroForAbmEvento = fecha
    }

    // ==================== BÚSQUEDAS Y LISTADOS ====================

    @Transactional(readOnly = true)
    fun getAllEventoByEmpresaId(empresaId: Long, pageNumber: Int): List<EventoDTO> {
        return eventoRepository.eventosByEmpresa(empresaId, PageRequest.of(pageNumber, 10)).content
    }

    @Transactional(readOnly = true)
    fun getAllEventosByFecha(empresa: Empresa): List<EventoDTO> {
        return eventoRepository.findAllByInicioBetweenAndEmpresa(
            LocalDateTime.parse(fechaFiltroForAbmEvento, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            LocalDateTime.parse(fechaFiltroForAbmEvento, DateTimeFormatter.ISO_LOCAL_DATE_TIME).plusDays(1),
            empresa)
    }

    @Transactional(readOnly = true)
    fun getAllEventoByFilterName(empresaId: Long, pageNumber: Int, buscar: String): List<EventoDTO> {
        return eventoRepository.eventosByNombre(empresaId, buscar, PageRequest.of(pageNumber, 10)).content
    }

    @Transactional(readOnly = true)
    fun cantEventos(empresaId: Long): Int {
        return eventoRepository.cantidadDeEventos(empresaId)
    }

    @Transactional(readOnly = true)
    fun cantEventosFiltrados(empresaId: Long, buscar: String): Int {
        return eventoRepository.cantidadDeEventosFiltrados(empresaId, buscar)
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["eventoVer"], key = "#eventoId")
    fun getEventoVer(eventoId: Long): EventoVerDTO? {
        return null
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["eventoExtra"], key = "#eventoId")
    fun getEventoExtra(eventoId: Long): EventoExtraDTO? {
        return null
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["eventoCatering"], key = "#eventoId")
    fun getEventoCatering(eventoId: Long): EventoCateringDTO? {
        return null
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["eventoHora"], key = "#eventoId")
    fun getEventoHora(eventoId: Long): EventoHoraDTO? {
        return null
    }

    @Transactional(readOnly = true)
    fun getPresupuesto(eventoId: Long): Double {
        return 0.0
    }

    @Transactional(readOnly = true)
    fun getAllEstado(): List<String> {
        return emptyList()
    }

    @Transactional(readOnly = true)
    fun getAllEstadoForSaveEvento(): List<String> {
        return emptyList()
    }

    @Transactional(readOnly = true)
    fun getByCodigoAndEmpresaId(codigo: String, empresaId: Long): Evento {
        return eventoRepository.getByCodigoAndEmpresaId(codigo, empresaId)
    }

    // ==================== EVENTOS POR USUARIO ====================

    @Transactional(readOnly = true)
    fun getEventosByUsuarioAndEmpresa(usuarioId: Long, empresaId: Long): List<String> {
        val eventos = eventoRepository.getEventosByUsuarioIdAndEmpresaId(usuarioId, empresaId)
        return eventos.map { it.nombre }
    }

    @Transactional(readOnly = true)
    fun getCantEventosByUsuarioAndEmpresa(usuarioId: Long, empresaId: Long): Int {
        return eventoRepository.getCantEventosByUsuarioIdAndEmpresaId(usuarioId, empresaId).toInt()
    }

    // ==================== DISPONIBILIDAD ====================

    @Transactional(readOnly = true)
    fun getListaEventoByDiaAndEmpresaId(evento: EventoBuscarFechaDTO): List<String> {
        return emptyList()
    }

    @Transactional(readOnly = true)
    fun getHorarioDisponible(evento: EventoBuscarFechaDTO): Boolean {
        return true
    }

    // ==================== CRUD ====================

    @Transactional
    @CacheEvict(value = ["eventoVer"], key = "#entity.id")
    override fun save(entity: Evento): Evento {
        return eventoRepository.save(entity)
    }

    // ==================== ACTUALIZAR INFORMACIÓN ====================

    @Transactional
    @CacheEvict(value = ["eventoHora"], key = "#evento.id")
    fun editEventoHora(evento: EventoHoraDTO): EventoHoraDTO {
        return evento
    }

    @Transactional
    @CacheEvict(value = ["eventoExtra"], key = "#evento.id")
    fun editEventoExtra(evento: EventoExtraDTO): EventoExtraDTO {
        return evento
    }

    @Transactional
    @CacheEvict(value = ["eventoCatering"], key = "#evento.id")
    fun editEventoCatering(evento: EventoCateringDTO): EventoCateringDTO {
        return evento
    }

    @Transactional
    fun editEventoAnotaciones(anotacion: String, id: Long): String {
        return anotacion
    }

    @Transactional
    fun editEventoCantNinos(eventoVer: EventoVerDTO): Int {
        return eventoVer.capacidad.capacidadNinos
    }

    @Transactional
    fun editEventoCantAdultos(eventoVer: EventoVerDTO): Int {
        return eventoVer.capacidad.capacidadAdultos
    }

    @Transactional
    fun editEventoNombre(nombre: String, id: Long): String {
        return nombre
    }

    @Transactional
    fun recorrerEspecificaciones(evento: Evento): Any {
        return emptyMap<String, Any>()
    }

    // ==================== COMUNICACIÓN ====================

    @Transactional
    fun reenviarMail(eventoId: Long): Boolean {
        return true
    }

    // ==================== DESCARGAS ====================

    @Transactional(readOnly = true)
    fun descargarEvento(): ByteArray {
        return ByteArray(0)
    }

    @Transactional(readOnly = true)
    fun generarEstadoDeCuentaPDF(): ByteArray {
        return ByteArray(0)
    }

    // ==================== AGENDA ====================

    @Transactional(readOnly = true)
    @Cacheable(value = ["eventoAgenda"], key = "#empresaId")
    fun getAllEventosForAgendaByEmpresaId(empresaId: Long): List<EventoAgendaDTO> {
        val desde = LocalDateTime.now()
        return eventoRepository.getAllEventosForAgendaByEmpresaId(empresaId, desde)
    }

    // ==================== DELETE ====================

    @Transactional
    @CacheEvict(value = ["eventoVer"], key = "#id")
    override fun delete(id: Long) {
        super.delete(id)
    }
}