package com.estonianport.agendaza.repository

import com.estonianport.agendaza.dto.ExtraDTO
import com.estonianport.agendaza.dto.ExtraPrecioDTO
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.enums.TipoExtra
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime

interface ExtraRepository : CrudRepository<Extra, Long> {

    // ==================== EVENTO ====================

    @Query("""
        SELECT COUNT(e) FROM Extra e
        JOIN e.listaEmpresa ee
        WHERE ee.id = :empresaId
          AND e.fechaBaja IS NULL
          AND e.tipoExtra IN (TipoExtra.EVENTO, TipoExtra.VARIABLE_EVENTO)
    """)
    fun countEvento(empresaId: Long): Int

    @Query("""
        SELECT COUNT(e) FROM Extra e
        JOIN e.listaEmpresa ee
        WHERE ee.id = :empresaId
          AND e.fechaBaja IS NULL
          AND e.nombre ILIKE %:buscar%
          AND e.tipoExtra IN (TipoExtra.EVENTO, TipoExtra.VARIABLE_EVENTO)
    """)
    fun countEventoByNombre(empresaId: Long, buscar: String): Int

    @Query("""
        SELECT e FROM Extra e
        JOIN e.listaEmpresa ee
        WHERE ee.id = :empresaId
          AND e.fechaBaja IS NULL
          AND e.tipoExtra IN (TipoExtra.EVENTO, TipoExtra.VARIABLE_EVENTO)
    """)
    fun findAllEvento(empresaId: Long, pageable: Pageable): Page<Extra>

    @Query("""
        SELECT e FROM Extra e
        JOIN e.listaEmpresa ee
        WHERE ee.id = :empresaId
          AND e.fechaBaja IS NULL
          AND e.nombre ILIKE %:buscar%
          AND e.tipoExtra IN (TipoExtra.EVENTO, TipoExtra.VARIABLE_EVENTO)
    """)
    fun findAllEventoByNombre(empresaId: Long, buscar: String, pageable: Pageable): Page<Extra>

    @Query("""
        SELECT new com.estonianport.agendaza.dto.ExtraDTO(e.id, e.nombre, e.tipoExtra)
        FROM Extra e
        WHERE e.fechaBaja IS NULL
          AND e.tipoExtra IN (TipoExtra.EVENTO, TipoExtra.VARIABLE_EVENTO)
    """)
    fun getAllEvento(): List<ExtraDTO>

    @Query("""
        SELECT new com.estonianport.agendaza.dto.ExtraDTO(e.id, e.nombre, e.tipoExtra)
        FROM Extra e
        WHERE e.fechaBaja IS NULL
          AND e.tipoExtra IN (TipoExtra.EVENTO, TipoExtra.VARIABLE_EVENTO)
          AND e.id NOT IN (
              SELECT e2.id FROM Extra e2
              JOIN e2.listaEmpresa emp
              WHERE emp.id = :empresaId
          )
        ORDER BY e.id DESC
        LIMIT 10
    """)
    fun getAllExtraEventoAgregar(empresaId: Long): List<ExtraDTO>

    // ==================== CATERING ====================

    @Query("""
        SELECT COUNT(e) FROM Extra e
        JOIN e.listaEmpresa ee
        WHERE ee.id = :empresaId
          AND e.fechaBaja IS NULL
          AND e.tipoExtra IN (TipoExtra.TIPO_CATERING, TipoExtra.VARIABLE_CATERING)
    """)
    fun countCatering(empresaId: Long): Int

    @Query("""
        SELECT COUNT(e) FROM Extra e
        JOIN e.listaEmpresa ee
        WHERE ee.id = :empresaId
          AND e.fechaBaja IS NULL
          AND e.nombre ILIKE %:buscar%
          AND e.tipoExtra IN (TipoExtra.TIPO_CATERING, TipoExtra.VARIABLE_CATERING)
    """)
    fun countCateringByNombre(empresaId: Long, buscar: String): Int

    @Query("""
        SELECT e FROM Extra e
        JOIN e.listaEmpresa ee
        WHERE ee.id = :empresaId
          AND e.fechaBaja IS NULL
          AND e.tipoExtra IN (TipoExtra.TIPO_CATERING, TipoExtra.VARIABLE_CATERING)
    """)
    fun findAllCatering(empresaId: Long, pageable: Pageable): Page<Extra>

    @Query("""
        SELECT e FROM Extra e
        JOIN e.listaEmpresa ee
        WHERE ee.id = :empresaId
          AND e.fechaBaja IS NULL
          AND e.nombre ILIKE %:buscar%
          AND e.tipoExtra IN (TipoExtra.TIPO_CATERING, TipoExtra.VARIABLE_CATERING)
    """)
    fun findAllCateringByNombre(empresaId: Long, buscar: String, pageable: Pageable): Page<Extra>

    @Query("""
        SELECT new com.estonianport.agendaza.dto.ExtraDTO(e.id, e.nombre, e.tipoExtra)
        FROM Extra e
        WHERE e.fechaBaja IS NULL
          AND e.tipoExtra IN (TipoExtra.TIPO_CATERING, TipoExtra.VARIABLE_CATERING)
    """)
    fun getAllCatering(): List<ExtraDTO>

    @Query("""
        SELECT new com.estonianport.agendaza.dto.ExtraDTO(e.id, e.nombre, e.tipoExtra)
        FROM Extra e
        WHERE e.fechaBaja IS NULL
          AND e.tipoExtra IN (TipoExtra.TIPO_CATERING, TipoExtra.VARIABLE_CATERING)
          AND e.id NOT IN (
              SELECT e2.id FROM Extra e2
              JOIN e2.listaEmpresa emp
              WHERE emp.id = :empresaId
          )
        ORDER BY e.id DESC
        LIMIT 10
    """)
    fun getAllExtraCateringAgregar(empresaId: Long): List<ExtraDTO>

    // ==================== PRECIO ====================

    @Query("""
        SELECT new com.estonianport.agendaza.dto.ExtraPrecioDTO(e.id, e.nombre, e.tipoExtra, COALESCE(p.precio, 0))
        FROM TipoEvento te
        JOIN te.listaExtra e
        LEFT JOIN precio_con_fecha_extra p
            ON p.extra.id = e.id
           AND p.empresa.id = :empresaId
           AND p.fechaBaja IS NULL
           AND p.desde <= :fechaEvento
           AND p.hasta >= :fechaEvento
        WHERE te.id = :tipoEventoId
          AND e.tipoExtra = :tipoExtra
          AND e.fechaBaja IS NULL
        ORDER BY e.nombre
    """)
    fun getAllExtraConPrecioByTipoEventoAndFecha(
        empresaId: Long,
        tipoEventoId: Long,
        fechaEvento: LocalDateTime,
        tipoExtra: TipoExtra
    ): List<ExtraPrecioDTO>
}