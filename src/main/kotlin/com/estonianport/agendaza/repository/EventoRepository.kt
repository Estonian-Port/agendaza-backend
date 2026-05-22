package com.estonianport.agendaza.repository

import com.estonianport.agendaza.dto.EventoAgendaDTO
import com.estonianport.agendaza.dto.EventoConUsuarioDTO
import com.estonianport.agendaza.dto.EventoDTO
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Evento
import com.estonianport.agendaza.model.Pago
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime
import java.util.*

interface EventoRepository : CrudRepository<Evento, Long>{

    override fun findAll() : List<Evento>

    @EntityGraph(attributePaths = ["tipoEvento", "encargado", "cliente", "capacidad", "listaExtra", "listaEventoExtraVariable"])
    override fun findById(id: Long) : Optional<Evento>

    fun findAllByInicioBetweenAndEmpresa(inicio: LocalDateTime, fin: LocalDateTime, empresa: Empresa): List<Evento>

    @Query("SELECT new com.estonianport.agendaza.dto.EventoDTO(" +
            "e.id, e.nombre, e.codigo, e.inicio, e.fin, e.tipoEvento.nombre) " +
            "FROM Evento e " +
            "WHERE e.empresa.id = ?1 AND e.fechaBaja IS NULL " +
            "ORDER BY e.inicio DESC")
    fun eventosByEmpresa(id : Long, pageable : Pageable) : Page<EventoDTO>

    @Query("SELECT COUNT(e) FROM Evento e WHERE e.empresa.id = ?1 AND e.fechaBaja IS NULL")
    fun cantidadDeEventos(id : Long) : Int

    @Query("SELECT new com.estonianport.agendaza.dto.EventoDTO(" +
            "e.id, e.nombre, e.codigo, e.inicio, e.fin, e.tipoEvento.nombre) " +
            "FROM Evento e " +
            "WHERE e.empresa.id = ?1 " +
            "AND (e.nombre ILIKE %?2% OR e.codigo ILIKE %?2%) " +
            "AND e.fechaBaja IS NULL " +
            "ORDER BY e.inicio DESC")
    fun eventosByNombre(id : Long, buscar : String, pageable : Pageable) : Page<EventoDTO>

    @Query("SELECT COUNT(e) FROM Evento e WHERE e.empresa.id = ?1 AND (e.nombre ILIKE %?2% OR e.codigo ILIKE %?2%) AND e.fechaBaja IS NULL")
    fun cantidadDeEventosFiltrados(id : Long, buscar: String) : Int

    @Query("SELECT new com.estonianport.agendaza.dto.EventoAgendaDTO(e.id, e.nombre, e.inicio, e.fin) FROM Evento e WHERE e.empresa.id = ?1 AND e.fechaBaja IS NULL")
    fun getAllEventosForAgendaByEmpresaId(empresaId : Long) : List<EventoAgendaDTO>

    @Query("""SELECT new com.estonianport.agendaza.dto.EventoConUsuarioDTO(
           e.id, e.nombre, e.codigo,
           e.cliente.id, e.cliente.nombre, e.cliente.apellido, e.cliente.username)
        FROM Evento e
        WHERE e.cliente.id = ?1 AND e.empresa.id = ?2 AND e.fechaBaja IS NULL 
        ORDER BY e.id DESC LIMIT 6""")
    fun getEventosByUsuarioIdAndEmpresaId(usuarioId: Long, empresaId: Long): List<EventoConUsuarioDTO>

    @Query("SELECT COUNT(e) FROM Evento e WHERE e.cliente.id = ?1 AND e.empresa.id = ?2 AND e.fechaBaja IS NULL")
    fun getCantEventosByUsuarioIdAndEmpresaId(usuarioId: Long, empresaId : Long): Int

    @Query("SELECT new com.estonianport.agendaza.dto.EventoDTO(e.id, e.nombre, e.codigo, e.inicio, e.fin, e.tipoEvento.nombre) FROM Evento e WHERE e.empresa.id = :empresaId AND e.inicio BETWEEN :fechaInicio AND :fechaFin AND e.fechaBaja IS NULL")
    fun getAllEventosForAgendaByFecha(fechaInicio: LocalDateTime, fechaFin : LocalDateTime, empresaId : Long): List<EventoDTO>

    @EntityGraph(attributePaths = ["tipoEvento", "encargado", "cliente", "capacidad", "listaExtra", "listaEventoExtraVariable"])
    @Query("SELECT e FROM Evento e WHERE e.codigo = ?1 AND e.empresa.id = ?2 AND e.fechaBaja IS NULL")
    fun getByCodigoAndEmpresaId(codigo : String, empresaId : Long) : Evento

    @Query("SELECT COUNT(ev) FROM Evento ev WHERE ev.empresa.id = :id AND ev.fechaBaja IS NULL")
    fun countActivosByEmpresaId(id: Long): Long

    @Query("SELECT COUNT(DISTINCT ev.cliente) FROM Evento ev WHERE ev.empresa.id = :id AND ev.cliente.fechaBaja IS NULL")
    fun countClientesByEmpresaId(id: Long): Long}