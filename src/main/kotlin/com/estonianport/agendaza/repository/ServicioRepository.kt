package com.estonianport.agendaza.repository

import com.estonianport.agendaza.dto.ServicioDTO
import com.estonianport.agendaza.model.Servicio
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface ServicioRepository : CrudRepository<Servicio, Long> {

    @Query("""
        SELECT new com.estonianport.agendaza.dto.ServicioDTO(s.id, s.nombre)
        FROM Servicio s
        JOIN s.listaEmpresa e
        WHERE e.id = :empresaId
          AND (CAST(:buscar AS string) IS NULL OR LOWER(s.nombre) LIKE LOWER(CONCAT('%', CAST(:buscar AS string), '%')))
        ORDER BY s.nombre ASC
    """)
    fun getPage(empresaId: Long, buscar: String?, pageable: Pageable): List<ServicioDTO>

    @Query("""
        SELECT COUNT(s) FROM Servicio s
        JOIN s.listaEmpresa e
        WHERE e.id = :empresaId
          AND (CAST(:buscar AS string) IS NULL OR LOWER(s.nombre) LIKE LOWER(CONCAT('%', CAST(:buscar AS string), '%')))
    """)
    fun count(empresaId: Long, buscar: String? = null): Int

    @Query("""
        SELECT new com.estonianport.agendaza.dto.ServicioDTO(s.id, s.nombre)
        FROM Servicio s
        WHERE s.nombre ILIKE %:q%
          AND s.id NOT IN (
              SELECT s2.id FROM Servicio s2
              JOIN s2.listaEmpresa emp
              WHERE emp.id = :empresaId
          )
        ORDER BY SIZE(s.listaEmpresa) DESC, s.nombre
    """)
    fun buscarParaAgregar(empresaId: Long, q: String, pageable: Pageable): List<ServicioDTO>

    fun findFirstByNombreIgnoreCaseOrderByIdAsc(nombre: String): Servicio?

    @Query("""
        SELECT COUNT(emp) FROM Servicio s
        JOIN s.listaEmpresa emp
        WHERE s.id = :servicioId AND emp.id <> :empresaId
    """)
    fun countOtrasEmpresas(servicioId: Long, empresaId: Long): Int

    // IDs de tipos de evento del servicio, solo de esa empresa
    @Query("""
        SELECT te.id FROM Servicio s
        JOIN s.listaTipoEvento te
        WHERE s.id = :servicioId AND te.empresa.id = :empresaId
    """)
    fun getTipoEventoIdsByServicioAndEmpresa(servicioId: Long, empresaId: Long): List<Long>

    @Query("""
        SELECT new com.estonianport.agendaza.dto.ServicioDTO(s.id, s.nombre)
        FROM Servicio s
        JOIN s.listaTipoEvento te
        WHERE te.id = :tipoEventoId
        ORDER BY s.nombre
    """)
    fun getAllServicioByTipoEventoId(tipoEventoId: Long): List<ServicioDTO>
}