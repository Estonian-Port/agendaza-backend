package com.estonianport.agendaza.repository

import com.estonianport.agendaza.dto.CantidadesPanelAdminDTO
import com.estonianport.agendaza.dto.PagoDTO
import com.estonianport.agendaza.dto.PrecioConFechaDTO
import com.estonianport.agendaza.dto.TipoEventoDTO
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Especificacion
import com.estonianport.agendaza.model.TipoEvento
import com.estonianport.agendaza.model.enums.Duracion
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

interface EmpresaRepository : CrudRepository<Empresa, Long>{

    @Query("SELECT e FROM Empresa e WHERE e.id = :empresaId")
    override fun findById(empresaId: Long) : Optional<Empresa>

    @Query("SELECT COUNT(c) FROM Cargo c WHERE c.empresa.id = :id AND c.fechaBaja IS NULL")
    fun countCargosByEmpresaId(id: Long): Long

    @Query("SELECT COUNT(te) FROM TipoEvento te WHERE te.empresa.id = :id AND te.fechaBaja IS NULL")
    fun countTipoEventoByEmpresaId(id: Long): Long

    @Query("SELECT COUNT(ex) FROM Extra ex JOIN ex.listaEmpresa e WHERE e.id = :id AND ex.fechaBaja IS NULL AND ex.tipoExtra IN :tipos")
    fun countExtraByEmpresaIdAndTipos(id: Long, tipos: List<String>): Long

    @Query("SELECT COUNT(p) FROM Pago p WHERE p.evento.empresa.id = :id AND p.fechaBaja IS NULL")
    fun countPagosByEmpresaId(id: Long): Long

    @Query("SELECT COUNT(ev) FROM Evento ev WHERE ev.empresa.id = :id AND ev.fechaBaja IS NULL")
    fun countEventosByEmpresaId(id: Long): Long

    @Query("SELECT COUNT(DISTINCT ev.cliente) FROM Evento ev WHERE ev.empresa.id = :id AND ev.cliente.fechaBaja IS NULL")
    fun countClientesDistintosActivosByEmpresaId(id: Long): Long

    @Query("SELECT COUNT(s) FROM Servicio s JOIN s.listaEmpresa e WHERE e.id = :id AND s.fechaBaja IS NULL")
    fun countServiciosByEmpresaId(id: Long): Long

    @Query("SELECT COUNT(c) FROM Clausula c JOIN c.listaEmpresa e WHERE e.id = :id")
    fun countClausulasByEmpresaId(id: Long): Long

    @Query("SELECT es FROM Empresa e INNER JOIN e.listaEspecificacion es WHERE e.id = :empresaId")
    fun getEspecificaciones(empresaId: Long): List<Especificacion>

    @Query("SELECT new com.estonianport.agendaza.dto.PrecioConFechaDTO(pf.id, pf.desde, pf.hasta, pf.precio, pf.empresa.id, pf.extra.id) " +
            "FROM Empresa e " +
            "INNER JOIN e.listaPrecioConFechaExtra pf " +
            "WHERE e.id = :empresaId AND pf.extra.id = :extraId " +
            " AND pf.desde >= CAST(CONCAT(EXTRACT(YEAR FROM CURRENT_DATE), '-01-01') AS DATE) " +
            " AND pf.fechaBaja IS NULL")
    fun getAllPrecioConFechaByExtraId(empresaId: Long, extraId: Long): List<PrecioConFechaDTO>

    @Query("SELECT new com.estonianport.agendaza.dto.PrecioConFechaDTO(pf.id, pf.desde, pf.hasta, pf.precio, pf.empresa.id, pf.tipoEvento.id) " +
            "FROM Empresa e " +
            "INNER JOIN e.listaPrecioConFechaTipoEvento pf " +
            "WHERE e.id = :empresaId AND pf.tipoEvento.id = :tipoEventoId " +
            " AND pf.desde >= CAST(CONCAT(EXTRACT(YEAR FROM CURRENT_DATE), '-01-01') AS DATE) " +
            " AND pf.fechaBaja IS NULL")
    fun getAllPrecioConFechaByTipoEventoId(empresaId: Long, tipoEventoId: Long): List<PrecioConFechaDTO>

    @Query(
        """
        SELECT new com.estonianport.agendaza.dto.TipoEventoDTO(
            te.id, 
            te.nombre, 
            te.cantidadDuracion, 
            te.duracion, 
            te.capacidad, 
            te.empresa.id
        ) 
        FROM TipoEvento te 
        WHERE te.empresa.id = :empresaId 
        AND te.duracion = :duracion 
        AND te.fechaBaja IS NULL
        """
    )
    fun findByEmpresaIdAndDuracion(empresaId: Long, duracion: Duracion): List<TipoEventoDTO>
}