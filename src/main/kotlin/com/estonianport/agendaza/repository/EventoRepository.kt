package com.estonianport.agendaza.repository

import com.estonianport.agendaza.dto.EventoAgendaDTO
import com.estonianport.agendaza.dto.EventoDTO
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Evento
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime
import java.util.*

interface EventoRepository : CrudRepository<Evento, Long>{

/*
    @EntityGraph(attributePaths = ["capacidad", "encargado", "cliente", "tipoEvento"])
*/
    override fun findAll() : List<Evento>

/*    @EntityGraph(attributePaths = [
        "capacidad",
        "cliente",
        "encargado",
        "tipoEvento"
    ])*/
    override fun findById(id: Long) : Optional<Evento>

    fun findAllByInicioBetweenAndEmpresa(inicio: LocalDateTime, fin: LocalDateTime, empresa: Empresa): List<Evento>

    @Query("SELECT e FROM Evento e WHERE e.empresa.id = ?1 AND e.fechaBaja IS NULL ORDER BY e.inicio DESC")
    fun eventosByEmpresa(id : Long, pageable : Pageable) : Page<Evento>

    @Query("SELECT COUNT(e) FROM Evento e WHERE e.empresa.id = ?1 AND e.fechaBaja IS NULL")
    fun cantidadDeEventos(id : Long) : Int

    @Query("SELECT e FROM Evento e WHERE e.empresa.id = ?1 AND (e.nombre ILIKE %?2% OR e.codigo ILIKE %?2%) AND e.fechaBaja IS NULL order by e.inicio desc")
    fun eventosByNombre(id : Long, buscar : String, pageable : Pageable) : Page<Evento>

    @Query("SELECT COUNT(e) FROM Evento e WHERE e.empresa.id = ?1 AND (e.nombre ILIKE %?2% OR e.codigo ILIKE %?2%) AND e.fechaBaja IS NULL")
    fun cantidadDeEventosFiltrados(id : Long, buscar: String) : Int

    @Query("SELECT new com.estonianport.agendaza.dto.EventoAgendaDTO(e.id, e.nombre, e.inicio, e.fin) FROM Evento e WHERE e.empresa.id = ?1 AND e.fechaBaja IS NULL")
    fun getAllEventosForAgendaByEmpresaId(empresaId : Long) : List<EventoAgendaDTO>

    @Query("SELECT e FROM Evento e WHERE e.cliente.id = ?1 AND e.empresa.id = ?2 AND e.fechaBaja IS NULL ORDER BY e.id DESC LIMIT 6")
    fun getEventosByUsuarioIdAndEmpresaId(usuarioId: Long, empresaId : Long): List<Evento>

    @Query("SELECT COUNT(e) FROM Evento e WHERE e.cliente.id = ?1 AND e.empresa.id = ?2 AND e.fechaBaja IS NULL")
    fun getCantEventosByUsuarioIdAndEmpresaId(usuarioId: Long, empresaId : Long): Int

    @Query("SELECT new com.estonianport.agendaza.dto.EventoDTO(e.id, e.nombre, e.codigo, e.inicio, e.fin, e.tipoEvento.nombre) FROM Evento e WHERE e.empresa.id = :empresaId AND e.inicio BETWEEN :fechaInicio AND :fechaFin AND e.fechaBaja IS NULL")
    fun getAllEventosForAgendaByFecha(fechaInicio: LocalDateTime, fechaFin : LocalDateTime, empresaId : Long): List<EventoDTO>

}