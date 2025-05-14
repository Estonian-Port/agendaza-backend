package com.estonianport.agendaza.repository

import com.estonianport.agendaza.dto.CantidadesPanelAdmin
import com.estonianport.agendaza.dto.PagoDTO
import com.estonianport.agendaza.dto.PrecioConFechaDto
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Especificacion
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

interface EmpresaRepository : CrudRepository<Empresa, Long>{

    @Query("SELECT e FROM Empresa e WHERE e.id = :empresaId")
    override fun findById(empresaId: Long) : Optional<Empresa>

    @Query("SELECT new com.estonianport.agendaza.dto.PagoDTO(p.id, p.monto, ev.codigo, p.medioDePago, " +
            "ev.nombre, p.fecha) FROM Pago p LEFT JOIN p.evento ev LEFT JOIN ev.empresa em " +
            "WHERE em.id = :empresaId")
    fun getEmpresaListaPagoById(empresaId: Long) : List<PagoDTO>

    @Query("SELECT new com.estonianport.agendaza.dto.CantidadesPanelAdmin (" +
            "(" +
            "SELECT COUNT(c) " +
            "FROM Cargo c " +
            "WHERE c.empresa.id = ?1" +
            "AND c.fechaBaja IS NULL), " +
            "(" +
            "SELECT COUNT(te) " +
            "FROM TipoEvento te " +
            "WHERE te.empresa.id = ?1), " +
            "(" +
            "SELECT COUNT(ex) " +
            "FROM Extra ex " +
            "INNER JOIN ex.listaEmpresa e " +
            "WHERE e.id = ?1 " +
            "   AND ex.fechaBaja IS NULL " +
            "   AND (ex.tipoExtra = 'EVENTO' " +
            "   OR ex.tipoExtra = 'VARIABLE_EVENTO')), " +
            "(" +
            "SELECT COUNT(p) " +
            "FROM Pago p " +
            "INNER JOIN Evento ev ON " +
            "   ev.id = p.evento.id " +
            "INNER JOIN Empresa e ON " +
            "   e.id = ev.empresa.id " +
            "WHERE e.id = ?1 " +
            "   AND p.fechaBaja IS NULL), " +
            "(" +
            "SELECT COUNT(ev) " +
            "FROM Evento ev " +
            "WHERE ev.empresa.id = ?1 " +
            "AND ev.fechaBaja IS NULL), " +
            "(" +
            "SELECT COUNT(DISTINCT ev.cliente) " +
            "FROM Evento ev " +
            "INNER JOIN Empresa e ON " +
            "   e.id = ev.empresa.id " +
            "INNER JOIN Usuario u ON " +
            "   u.id = ev.cliente.id " +
            "WHERE e.id = ?1 " +
            "AND u.fechaBaja IS NULL), " +
            "(" +
            "SELECT COUNT(ex) " +
            "FROM Extra ex " +
            "INNER JOIN ex.listaEmpresa e " +
            "WHERE e.id = ?1 " +
            "   AND ex.fechaBaja IS NULL " +
            "   AND (ex.tipoExtra = 'TIPO_CATERING' " +
            "   OR ex.tipoExtra = 'VARIABLE_CATERING')), " +
            "(" +
            "SELECT COUNT(s) " +
            "FROM Servicio s " +
            "INNER JOIN s.listaEmpresa e " +
            "WHERE e.id = ?1 " +
            "   AND s.fechaBaja IS NULL ), " +
            "(" +
            "SELECT COUNT(es) " +
            "FROM Especificacion es " +
            "WHERE es.empresa.id = ?1)) " +
            "FROM Empresa e " +
            "WHERE e.id = ?1")
    fun getAllCantidadesForPanelAdminByEmpresaId(id : Long) : CantidadesPanelAdmin

    @Query("SELECT es FROM Empresa e INNER JOIN e.listaEspecificacion es WHERE e.id = :empresaId")
    fun getEspecificaciones(empresaId: Long): List<Especificacion>

    @Query("SELECT new com.estonianport.agendaza.dto.PrecioConFechaDto(pf.id, pf.desde, pf.hasta, pf.precio, pf.empresa.id, pf.extra.id) " +
            "FROM Empresa e " +
            "INNER JOIN e.listaPrecioConFechaExtra pf " +
            "WHERE e.id = :empresaId AND pf.extra.id = :extraId " +
            " AND pf.desde >= CAST(CONCAT(EXTRACT(YEAR FROM CURRENT_DATE), '-01-01') AS DATE) " +
            " AND pf.fechaBaja IS NULL")
    fun getAllPrecioConFechaByExtraId(empresaId: Long, extraId: Long): List<PrecioConFechaDto>

}