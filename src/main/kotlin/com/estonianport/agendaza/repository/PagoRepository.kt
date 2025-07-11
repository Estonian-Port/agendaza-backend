package com.estonianport.agendaza.repository

import com.estonianport.agendaza.dto.PagoDTO
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.Pago
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface PagoRepository : CrudRepository<Pago, Long>{

    @Query("SELECT COUNT(p) FROM Pago p WHERE p.evento.empresa.id = ?1 AND p.fechaBaja IS NULL")
    fun cantidadPagos(id : Long) : Int

    @Query("SELECT COUNT(p) FROM Pago p WHERE p.evento.empresa.id = ?1 AND (p.evento.codigo ILIKE %?2% OR p.evento.cliente.nombre ILIKE %?2%) AND p.fechaBaja IS NULL")
    fun cantidadPagosFiltrados(id : Long, buscar: String) : Int

    @Query("SELECT p FROM Pago p WHERE p.evento.empresa.id = ?1 AND p.fechaBaja IS NULL ORDER BY p.fecha DESC")
    fun findAll(id: Long,  pageable : Pageable) : Page<Pago>

    @Query("SELECT p FROM Pago p WHERE p.evento.empresa.id = ?1 AND (p.evento.codigo ILIKE %?2% OR p.evento.cliente.nombre ILIKE %?2%) AND p.fechaBaja IS NULL ORDER BY p.fecha DESC")
    fun pagosByNombre(id : Long, buscar : String, pageable : Pageable) : Page<Pago>

    @Query("SELECT new com.estonianport.agendaza.dto.PagoDTO(p.id, p.monto, p.concepto, cast(null as string), ev.codigo, p.medioDePago," +
            "ev.nombre, p.evento.inicio, p.fecha, ev.empresa.id, p.encargado.id) FROM Pago p LEFT JOIN p.evento ev WHERE p.evento.id = :eventoId AND p.fechaBaja IS NULL ORDER BY p.fecha DESC")
    fun getAllPagoFromEvento(eventoId: Long) : List<PagoDTO>

}