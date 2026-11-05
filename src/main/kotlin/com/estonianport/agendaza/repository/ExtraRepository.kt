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

    // ====================  PAGINADO ====================

    @Query("""
        SELECT new com.estonianport.agendaza.dto.ExtraDTO(e.id, e.nombre, e.tipoExtra)
        FROM Extra e        
        JOIN e.listaEmpresa ee
        WHERE ee.id = :empresaId
          AND e.fechaBaja IS NULL
          AND e.tipoExtra IN :tiposExtra
          AND (CAST(:buscar AS string) IS NULL OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', CAST(:buscar AS string), '%')))
        ORDER BY e.nombre ASC
""")
    fun getPage(
        empresaId: Long,
        tiposExtra: List<TipoExtra>,
        buscar: String?,
        pageable: Pageable
    ): List<ExtraDTO>

    @Query("""
        SELECT COUNT(e) FROM Extra e
        JOIN e.listaEmpresa ee
        WHERE ee.id = :empresaId
          AND e.fechaBaja IS NULL
          AND e.tipoExtra IN :tiposExtra
          AND (CAST(:buscar AS string) IS NULL OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', CAST(:buscar AS string), '%')))
    """)
    fun count(
        empresaId: Long,
        tiposExtra: List<TipoExtra>,
        buscar: String? = null
    ): Int

    // ==================== BUSQUEDA ====================

    @Query("""
    SELECT new com.estonianport.agendaza.dto.ExtraDTO(e.id, e.nombre, e.tipoExtra)
    FROM Extra e
    WHERE e.fechaBaja IS NULL
      AND e.tipoExtra IN :tipos
      AND e.nombre ILIKE %:q%
      AND e.id NOT IN (
          SELECT e2.id FROM Extra e2
          JOIN e2.listaEmpresa emp
          WHERE emp.id = :empresaId
      )
    ORDER BY SIZE(e.listaEmpresa) DESC, e.nombre
""")
    fun buscarParaAgregar(
        empresaId: Long, tipos: Collection<TipoExtra>, q: String, pageable: Pageable
    ): List<ExtraDTO>

    fun findFirstByTipoExtraAndNombreIgnoreCaseOrderByIdAsc(tipoExtra: TipoExtra, nombre: String): Extra?

    // ==================== PRECIO ====================

    @Query("""
        SELECT new com.estonianport.agendaza.dto.ExtraPrecioDTO(e.id, e.nombre, e.tipoExtra, COALESCE(p.precio, 0))
        FROM TipoEvento te
        JOIN te.listaExtra e
        JOIN e.listaEmpresa empresa
        LEFT JOIN precio_con_fecha_extra p
            ON p.extra.id = e.id
           AND p.empresa.id = :empresaId
           AND p.fechaBaja IS NULL
           AND p.desde <= :fechaEvento
           AND p.hasta >= :fechaEvento
        WHERE te.id = :tipoEventoId
          AND empresa.id = :empresaId
          AND e.tipoExtra IN :tiposExtra
          AND e.fechaBaja IS NULL
        ORDER BY e.nombre ASC
    """)
    fun getAllExtraConPrecioByTipoEventoAndFecha(
        empresaId: Long,
        tipoEventoId: Long,
        fechaEvento: LocalDateTime,
        tiposExtra: List<TipoExtra>
    ): List<ExtraPrecioDTO>

    @Query("""
    SELECT COUNT(emp) FROM Extra e
    JOIN e.listaEmpresa emp
    WHERE e.id = :extraId
      AND emp.id <> :empresaId
""")
    fun countOtrasEmpresas(extraId: Long, empresaId: Long): Int
}
