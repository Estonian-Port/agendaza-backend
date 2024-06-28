package com.estonianport.agendaza.repository

import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.Usuario
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import com.estonianport.agendaza.model.TipoExtra

interface ExtraRepository : CrudRepository<Extra, Long>{

    @Query("SELECT COUNT(e) FROM Extra e WHERE e.empresa.id = ?1  AND e.fechaBaja IS NULL AND (e.tipoExtra = TipoExtra.EVENTO OR e.tipoExtra = TipoExtra.VARIABLE_EVENTO)")
    fun cantidadExtras(id : Long) : Int

    @Query("SELECT COUNT(e) FROM Extra e WHERE e.empresa.id = ?1  AND e.fechaBaja IS NULL AND e.nombre ILIKE %?2% and (e.tipoExtra = TipoExtra.EVENTO OR e.tipoExtra = TipoExtra.VARIABLE_EVENTO)")
    fun cantidadExtrasFiltrados(id : Long, buscar: String) : Int

    @Query(value = "select e from Extra e where e.empresa.id = ?1 AND e.fechaBaja IS NULL AND (e.tipoExtra = TipoExtra.EVENTO OR e.tipoExtra = TipoExtra.VARIABLE_EVENTO)")
    fun findAll(id: Long,  pageable : Pageable) : Page<Extra>

    @Query(value = "SELECT e FROM Extra e WHERE e.empresa.id = ?1  AND e.fechaBaja IS NULL AND e.nombre ILIKE %?2% and (e.tipoExtra = TipoExtra.EVENTO OR e.tipoExtra = TipoExtra.VARIABLE_EVENTO)")
    fun extrasByNombre(id : Long, buscar : String, pageable : Pageable) : Page<Extra>

    @Query("SELECT COUNT(e) FROM Extra e WHERE e.empresa.id = ?1  AND e.fechaBaja IS NULL AND (e.tipoExtra = TipoExtra.TIPO_CATERING OR e.tipoExtra = TipoExtra.VARIABLE_CATERING)")
    fun cantidadExtrasCAT(id : Long) : Int

    @Query("SELECT COUNT(e) FROM Extra e WHERE e.empresa.id = ?1  AND e.fechaBaja IS NULL AND e.nombre ILIKE %?2% and (e.tipoExtra = TipoExtra.TIPO_CATERING OR e.tipoExtra = TipoExtra.VARIABLE_CATERING)")
    fun cantidadExtrasCATFiltrados(id : Long, buscar: String) : Int

    @Query(value = "select e from Extra e where e.empresa.id = ?1 AND e.fechaBaja IS NULL AND (e.tipoExtra = TipoExtra.TIPO_CATERING OR e.tipoExtra = TipoExtra.VARIABLE_CATERING)")
    fun findAllCAT(id: Long,  pageable : Pageable) : Page<Extra>

    @Query(value = "SELECT e FROM Extra e WHERE e.empresa.id = ?1  AND e.fechaBaja IS NULL AND e.nombre ILIKE %?2% and (e.tipoExtra = TipoExtra.TIPO_CATERING OR e.tipoExtra = TipoExtra.VARIABLE_CATERING)")
    fun extrasCATByNombre(id : Long, buscar : String, pageable : Pageable) : Page<Extra>
}

