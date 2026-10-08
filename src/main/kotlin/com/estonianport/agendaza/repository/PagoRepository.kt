package com.estonianport.agendaza.repository

import com.estonianport.agendaza.dto.EventoPagoDTO
import com.estonianport.agendaza.dto.PagoDTO
import com.estonianport.agendaza.dto.TotalesPagosMes
import com.estonianport.agendaza.model.Pago
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime

interface PagoRepository : CrudRepository<Pago, Long> {

    @Query(
        """
        SELECT new com.estonianport.agendaza.dto.EventoPagoDTO(ev.id, ev.nombre, ev.codigo, 0)
        FROM Evento ev
        WHERE ev.id = :eventoId AND ev.fechaBaja IS NULL"""
    )
    fun getEventoForPago(eventoId: Long): EventoPagoDTO?

    @Query(
        "SELECT new com.estonianport.agendaza.dto.PagoDTO(p.id, p.monto, p.concepto, cast(null as string), ev.codigo, p.medioDePago," +
                "ev.nombre, p.evento.inicio, p.fecha, ev.empresa.id, p.encargado.id) FROM Pago p LEFT JOIN p.evento ev WHERE p.evento.id = :eventoId AND p.fechaBaja IS NULL ORDER BY p.fecha DESC"
    )
    fun getAllPagoFromEvento(eventoId: Long): List<PagoDTO>?

    @Query(
        """
    SELECT new com.estonianport.agendaza.dto.PagoDTO(
        0L, 0.0, null, cast(null as string),
        ev.codigo, null,
        ev.nombre, ev.inicio, :fechaAhora,
        ev.empresa.id, ev.encargado.id
    )
    FROM Evento ev
    WHERE ev.id = :eventoId AND ev.fechaBaja IS NULL"""
    )
    fun getEventoForSavePago(eventoId: Long, fechaAhora: LocalDateTime): PagoDTO?

    @Query("""
    SELECT COUNT(DISTINCT p) 
    FROM Pago p 
    JOIN p.evento e 
    WHERE e.empresa.id = :id 
    AND p.fechaBaja IS NULL"""
    )
    fun countActivosByEmpresaId(id: Long): Int

    @Query(
        """
        SELECT new com.estonianport.agendaza.dto.PagoDTO(p.id, p.monto, p.concepto, cast(null as string), ev.codigo, p.medioDePago,
            ev.nombre, ev.inicio, p.fecha, ev.empresa.id, p.encargado.id)
        FROM Pago p JOIN p.evento ev
        WHERE ev.empresa.id = :empresaId
          AND p.fechaBaja IS NULL
          AND p.fecha >= :desde AND p.fecha < :hasta
        ORDER BY p.fecha DESC"""
    )
    fun getAllPagoByRango(empresaId: Long, desde: LocalDateTime, hasta: LocalDateTime): List<PagoDTO>

    @Query(
        """
        SELECT new com.estonianport.agendaza.dto.TotalesPagosMes(COALESCE(SUM(p.monto), 0.0), COUNT(p))
        FROM Pago p JOIN p.evento ev
        WHERE ev.empresa.id = :empresaId
          AND p.fechaBaja IS NULL
          AND p.fecha >= :desde AND p.fecha < :hasta"""
    )
    fun totalesByRango(empresaId: Long, desde: LocalDateTime, hasta: LocalDateTime): TotalesPagosMes

    @Query("""
    SELECT p FROM Pago p
    JOIN FETCH p.evento ev
    JOIN FETCH ev.cliente
    WHERE ev.empresa.id = :empresaId
      AND p.fechaBaja IS NULL
      AND p.fecha >= :desde AND p.fecha < :hasta""")
    fun findParaPlanilla(empresaId: Long, desde: LocalDateTime, hasta: LocalDateTime): List<Pago>
}