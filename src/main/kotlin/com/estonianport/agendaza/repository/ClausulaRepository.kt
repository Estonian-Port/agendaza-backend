package com.estonianport.agendaza.repository

import com.estonianport.agendaza.dto.GenericItemDTO
import com.estonianport.agendaza.model.Pago
import com.estonianport.agendaza.model.Clausula
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface ClausulaRepository : CrudRepository<Clausula, Long> {

    @Query("SELECT new com.estonianport.agendaza.dto.GenericItemDTO(c.id, c.nombre) FROM Clausula c " +
            "INNER JOIN c.listaEmpresa e WHERE e.id = :empresaId AND c.fechaBaja IS NULL ORDER BY c.id DESC")
    fun getAll(empresaId: Long, pageable: Pageable): Page<GenericItemDTO>

    @Query("SELECT COUNT(c) FROM Clausula c INNER JOIN c.listaEmpresa e WHERE e.id = :empresaId " +
            "AND c.fechaBaja IS NULL")
    fun getAllCantidad(empresaId: Long): Int

    @Query("SELECT new com.estonianport.agendaza.dto.GenericItemDTO(c.id, c.nombre) FROM Clausula c " +
            "INNER JOIN c.listaEmpresa e WHERE e.id = :empresaId AND c.nombre ILIKE %:buscar% AND c.fechaBaja IS NULL ORDER BY c.id DESC")
    fun getAllFiltro(empresaId: Long, buscar: String, pageable: Pageable): Page<GenericItemDTO>

    @Query("SELECT COUNT(c) FROM Clausula c INNER JOIN c.listaEmpresa e WHERE e.id = :empresaId " +
            "AND c.nombre ILIKE %:buscar% AND c.fechaBaja IS NULL")
    fun getAllCantidadFiltro(empresaId: Long, buscar: String): Int

    @Query("""SELECT new com.estonianport.agendaza.dto.GenericItemDTO(c.id, c.nombre)
        FROM Clausula c
        LEFT JOIN c.listaEmpresa e WITH e.id = :empresaId
        WHERE e.id IS NULL AND c.fechaBaja IS NULL
        ORDER BY c.id DESC
        LIMIT 10""")
    fun getAllAgregar(empresaId: Long): List<GenericItemDTO>

}