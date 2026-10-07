package com.estonianport.agendaza.repository

import com.estonianport.agendaza.dto.GastoDTO
import com.estonianport.agendaza.dto.TotalesPagosMes
import com.estonianport.agendaza.model.Gasto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime

interface GastoRepository : CrudRepository<Gasto, Long> {

    @Query(
        """
        SELECT new com.estonianport.agendaza.dto.GastoDTO(
            g.id, g.monto, g.tipoGasto, g.descripcion, g.fecha, e.id, g.empresa.id, g.encargado.id,
            g.medioDePago, e.nombre, e.codigo
        )
        FROM Gasto g
        LEFT JOIN g.evento e
        WHERE g.empresa.id = :empresaId
          AND g.fechaBaja IS NULL
          AND g.fecha >= :desde AND g.fecha < :hasta
        ORDER BY g.fecha DESC"""
    )
    fun getAllGastoByRango(empresaId: Long, desde: LocalDateTime, hasta: LocalDateTime): List<GastoDTO>

    @Query(
        """
        SELECT new com.estonianport.agendaza.dto.TotalesPagosMes(COALESCE(SUM(g.monto), 0.0), COUNT(g))
        FROM Gasto g
        WHERE g.empresa.id = :empresaId
          AND g.fechaBaja IS NULL
          AND g.fecha >= :desde AND g.fecha < :hasta"""
    )
    fun totalesByRango(empresaId: Long, desde: LocalDateTime, hasta: LocalDateTime): TotalesPagosMes
}