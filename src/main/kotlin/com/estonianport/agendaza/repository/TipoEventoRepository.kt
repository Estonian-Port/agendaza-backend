package com.estonianport.agendaza.repository

import com.estonianport.agendaza.dto.TipoEventoDTO
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.TipoEvento
import com.estonianport.agendaza.model.TipoExtra
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface TipoEventoRepository : CrudRepository<TipoEvento, Long> {
    override fun findAll(): List<TipoEvento>

    @Query("SELECT new com.estonianport.agendaza.dto.TipoEventoDTO(t.id, t.nombre, t.cantidadDuracion, " +
            "t.duracion, t.capacidad, t.empresa.id) FROM TipoEvento t INNER JOIN t.listaExtra e " +
            "where e.id = :extraId")
    fun getAllByExtra(extraId : Long): MutableList<TipoEventoDTO>

    @Query("SELECT new com.estonianport.agendaza.dto.TipoEventoDTO(t.id, t.nombre, t.cantidadDuracion, " +
            "t.duracion, t.capacidad, t.empresa.id) FROM TipoEvento t INNER JOIN t.listaServicio s " +
            "where s.id = :servicioId")
    fun getAllByServicio(servicioId: Long): MutableList<TipoEventoDTO>


    @Query("SELECT new com.estonianport.agendaza.dto.TipoEventoDTO(t.id, t.nombre, t.cantidadDuracion, " +
            "t.duracion, t.capacidad, t.empresa.id) FROM TipoEvento t INNER JOIN t.empresa ee WHERE ee.id = :empresaId " +
            "AND t.fechaBaja IS NULL ORDER BY t.id DESC")
    fun getAllTipoEventoByEmpresaId(empresaId: Long) : List<TipoEventoDTO>

    @Query("SELECT new com.estonianport.agendaza.dto.TipoEventoDTO(t.id, t.nombre, t.cantidadDuracion, " +
            "t.duracion, t.capacidad, t.empresa.id) FROM TipoEvento t INNER JOIN t.empresa ee WHERE ee.id = :empresaId " +
            "AND t.fechaBaja IS NULL ORDER BY t.id DESC")
    fun getAllTipoEventoByEmpresaId(empresaId: Long, pageable : Pageable) : Page<TipoEventoDTO>

    @Query("SELECT COUNT(t) FROM TipoEvento t INNER JOIN t.empresa ee WHERE ee.id = :empresaId AND t.fechaBaja IS NULL")
    fun getCantidadTipoEvento(empresaId : Long) : Int

    @Query("SELECT new com.estonianport.agendaza.dto.TipoEventoDTO(t.id, t.nombre, t.cantidadDuracion, " +
            "t.duracion, t.capacidad, t.empresa.id) FROM TipoEvento t INNER JOIN t.empresa ee WHERE ee.id = :empresaId " +
            "AND t.nombre ILIKE %:buscar% AND t.fechaBaja IS NULL ORDER BY t.id DESC")
    fun getAllTipoEventoFilterNombre(empresaId : Long, buscar : String, pageable : Pageable) : Page<TipoEventoDTO>

    @Query("SELECT COUNT(t) FROM TipoEvento t INNER JOIN t.empresa ee WHERE ee.id = :empresaId  " +
            "AND t.nombre ILIKE %:buscar% AND t.fechaBaja IS NULL")
    fun getCantidadTipoEventoFiltrados(empresaId : Long, buscar: String) : Int

    @Query("SELECT tee FROM TipoEvento te INNER JOIN te.listaExtra tee WHERE te.id = :tipoEventoId " +
            "AND tee.tipoExtra = :tipoExtra AND tee.fechaBaja IS NULL")
    fun getAllExtraByTipoExtra(tipoEventoId: Long, tipoExtra: TipoExtra): List<Extra>

}