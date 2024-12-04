package com.estonianport.agendaza.repository

import com.estonianport.agendaza.dto.ServicioDTO
import com.estonianport.agendaza.dto.TipoEventoDTO
import com.estonianport.agendaza.model.TipoEvento
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
            "t.duracion, t.capacidad, t.empresa.id) FROM TipoEvento t WHERE t.empresa.id = :empresaId " +
            "AND t.fechaBaja IS NULL ORDER BY t.id DESC")
    fun getAllTipoEventoByEmpresaId(empresaId: Long, pageable : Pageable) : Page<TipoEventoDTO>

    @Query("SELECT COUNT(t) FROM TipoEvento t INNER JOIN s.listaEmpresa e WHERE e.id = :empresaId " +
            "AND t.fechaBaja IS NULL")
    fun getCantidadTipoEvento(empresaId : Long) : Int

    @Query("SELECT new com.estonianport.agendaza.dto.TipoEventoDTO(t.id, t.nombre, t.cantidadDuracion, " +
            "t.duracion, t.capacidad, t.empresa.id) FROM TipoEvento t WHERE t.empresa.id = :empresaId " +
            "AND t.nombre ILIKE %:buscar% AND t.fechaBaja IS NULL ORDER BY t.id DESC")
    fun getAllTipoEventoFilterNombre(empresaId : Long, buscar : String, pageable : Pageable) : Page<TipoEventoDTO>

    @Query("SELECT COUNT(s) FROM TipoEvento s INNER JOIN s.listaEmpresa e WHERE e.id = :empresaId " +
            "AND s.nombre ILIKE %:buscar% AND s.fechaBaja IS NULL")
    fun getCantidadTipoEventoFiltrados(empresaId : Long, buscar: String) : Int
}