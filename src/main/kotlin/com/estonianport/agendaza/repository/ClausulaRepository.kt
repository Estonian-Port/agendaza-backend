package com.estonianport.agendaza.repository

import com.estonianport.agendaza.dto.GenericItemDTO
import com.estonianport.agendaza.model.Clausula
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface ClausulaRepository : CrudRepository<Clausula, Long> {

    @Query("""
        SELECT new com.estonianport.agendaza.dto.GenericItemDTO(c.id, c.nombre)
        FROM Clausula c
        JOIN c.listaEmpresa e
        WHERE e.id = :empresaId
          AND c.fechaBaja IS NULL
          AND (CAST(:buscar AS string) IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', CAST(:buscar AS string), '%')))
        ORDER BY c.id DESC
    """)
    fun getPage(empresaId: Long, buscar: String?, pageable: Pageable): List<GenericItemDTO>

    @Query("""
        SELECT COUNT(c)
        FROM Clausula c
        JOIN c.listaEmpresa e
        WHERE e.id = :empresaId
          AND c.fechaBaja IS NULL
          AND (CAST(:buscar AS string) IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', CAST(:buscar AS string), '%')))
    """)
    fun count(empresaId: Long, buscar: String?): Int

    @Query("SELECT COUNT(c) FROM Clausula c JOIN c.listaEmpresa e WHERE e.id = :id")
    fun countByEmpresaId(id: Long): Int
}
