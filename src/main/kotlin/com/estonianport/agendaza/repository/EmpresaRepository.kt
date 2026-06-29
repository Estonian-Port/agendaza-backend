package com.estonianport.agendaza.repository

import com.estonianport.agendaza.dto.EmpresaDTO
import com.estonianport.agendaza.dto.PrecioConFechaDTO
import com.estonianport.agendaza.dto.TipoEventoDTO
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Especificacion
import com.estonianport.agendaza.model.enums.Duracion
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

interface EmpresaRepository : CrudRepository<Empresa, Long>{

    @Query("SELECT e FROM Empresa e WHERE e.id = :empresaId")
    override fun findById(empresaId: Long) : Optional<Empresa>

    @Query("""
        SELECT new com.estonianport.agendaza.dto.EmpresaDTO(
            e.id, e.nombre, e.email, e.telefono, e.calle, e.numero, e.municipio
        )
        FROM Empresa e 
        WHERE e.id = :empresaId
    """)
    fun findDTOById(empresaId: Long): Optional<EmpresaDTO>

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
            te.capacidadAdultos,
            te.capacidadNinos,
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