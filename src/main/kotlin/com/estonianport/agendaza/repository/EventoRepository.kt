package com.estonianport.agendaza.repository

import com.estonianport.agendaza.dto.EventoAgendaDTO
import com.estonianport.agendaza.dto.EventoConUsuarioDTO
import com.estonianport.agendaza.dto.EventoDTO
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Evento
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface EventoRepository : CrudRepository<Evento, Long> {

    override fun findAll(): List<Evento>

    /**
     * Busca un evento por ID con todas sus relaciones cargadas (eager loading)
     */
    @EntityGraph(attributePaths = [
        "tipoEvento",
        "encargado",
        "cliente",
        "capacidad",
        "listaExtra",
        "listaEventoExtraVariable",
        "empresa"
    ])
    override fun findById(id: Long): Optional<Evento>

    /**
     * Busca eventos en un rango de fechas para una empresa específica
     * Utilizado para validar disponibilidad de horarios
     */
    @Query(
        """
    SELECT new com.estonianport.agendaza.dto.EventoDTO(
        e.id, e.nombre, e.codigo, e.inicio, e.fin, e.tipoEvento.nombre
    )
    FROM Evento e 
    WHERE e.empresa.id = :empresaId 
    AND e.inicio BETWEEN :inicio AND :fin
    AND e.fechaBaja IS NULL
    """
    )
    fun findAllByInicioBetweenAndEmpresa(
        inicio: LocalDateTime,
        fin: LocalDateTime,
        empresaId: Long
    ): List<EventoDTO>

    /**
     * Verifica si hay superposición de horarios directamente en la BD.
     * Retorna TRUE si encuentra al menos un evento que se solape con los márgenes dados.
     */
    @Query(
        """
        SELECT COUNT(e) > 0 
        FROM Evento e 
        WHERE e.empresa.id = :empresaId 
        AND e.fechaBaja IS NULL
        AND (e.inicio < :finMargen AND e.fin > :inicioMargen)
        """
    )
    fun existeSuperposicionDeHorarios(
        empresaId: Long,
        inicioMargen: LocalDateTime,
        finMargen: LocalDateTime
    ): Boolean

    // ==================== BÚSQUEDAS Y LISTADOS ====================

    /**
     * Obtiene eventos activos de una empresa sin filtro, paginados
     */
    @Query(
        """
        SELECT new com.estonianport.agendaza.dto.EventoDTO(
            e.id, e.nombre, e.codigo, e.inicio, e.fin, e.tipoEvento.nombre
        )
        FROM Evento e
        WHERE e.empresa.id = :empresaId
        AND e.fechaBaja IS NULL
        ORDER BY e.inicio DESC
        """
    )
    fun eventosByEmpresa(empresaId: Long, pageable: Pageable): Page<EventoDTO>

    /**
     * Obtiene eventos de una empresa filtrados por nombre o código
     */
    @Query(
        """
        SELECT new com.estonianport.agendaza.dto.EventoDTO(
            e.id, e.nombre, e.codigo, e.inicio, e.fin, e.tipoEvento.nombre
        )
        FROM Evento e
        WHERE e.empresa.id = :empresaId
        AND (LOWER(e.nombre) LIKE LOWER(CONCAT('%', :buscar, '%'))
             OR LOWER(e.codigo) LIKE LOWER(CONCAT('%', :buscar, '%')))
        AND e.fechaBaja IS NULL
        ORDER BY e.inicio DESC
        """
    )
    fun eventosByNombre(empresaId: Long, buscar: String, pageable: Pageable): Page<EventoDTO>

    /**
     * Cuenta eventos activos de una empresa
     */
    @Query(
        """
        SELECT COUNT(e)
        FROM Evento e
        WHERE e.empresa.id = :empresaId
        AND e.fechaBaja IS NULL
        """
    )
    fun cantidadDeEventos(empresaId: Long): Int

    /**
     * Cuenta eventos filtrados por búsqueda en una empresa
     */
    @Query(
        """
        SELECT COUNT(e)
        FROM Evento e
        WHERE e.empresa.id = :empresaId
        AND (LOWER(e.nombre) LIKE LOWER(CONCAT('%', :buscar, '%'))
             OR LOWER(e.codigo) LIKE LOWER(CONCAT('%', :buscar, '%')))
        AND e.fechaBaja IS NULL
        """
    )
    fun cantidadDeEventosFiltrados(empresaId: Long, buscar: String): Int

    // ==================== AGENDA Y CALENDARIO ====================

    /**
     * Obtiene eventos para la vista de agenda (últimos 12 meses hacia adelante)
     * Utilizado para mostrar el calendario de eventos
     */
    @Query(
        """
        SELECT new com.estonianport.agendaza.dto.EventoAgendaDTO(
            e.id, e.nombre, e.inicio, e.fin
        )
        FROM Evento e
        WHERE e.empresa.id = :empresaId
        AND e.fechaBaja IS NULL
        AND e.inicio >= :desde
        ORDER BY e.inicio ASC
        """
    )
    fun getAllEventosForAgendaByEmpresaId(
        empresaId: Long,
        desde: LocalDateTime
    ): List<EventoAgendaDTO>

    /**
     * Obtiene eventos de una empresa en una fecha específica
     * Utilizado para buscar eventos en un día específico
     */
    @Query(
        """
        SELECT new com.estonianport.agendaza.dto.EventoDTO(
            e.id, e.nombre, e.codigo, e.inicio, e.fin, e.tipoEvento.nombre
        )
        FROM Evento e
        WHERE e.empresa.id = :empresaId
        AND e.inicio BETWEEN :fechaInicio AND :fechaFin
        AND e.fechaBaja IS NULL
        ORDER BY e.inicio ASC
        """
    )
    fun getAllEventosForAgendaByFecha(
        fechaInicio: LocalDateTime,
        fechaFin: LocalDateTime,
        empresaId: Long
    ): List<EventoDTO>

    // ==================== BÚSQUEDAS POR USUARIO/CLIENTE ====================

    /**
     * Obtiene eventos contratados por un usuario/cliente en una empresa
     * Limitado a los últimos 6 eventos
     */
    @Query(
        """
        SELECT new com.estonianport.agendaza.dto.EventoConUsuarioDTO(
            e.id, e.nombre, e.codigo,
            e.cliente.id, e.cliente.nombre, e.cliente.apellido, e.cliente.username
        )
        FROM Evento e
        WHERE e.cliente.id = :usuarioId
        AND e.empresa.id = :empresaId
        AND e.fechaBaja IS NULL
        ORDER BY e.id DESC
        LIMIT 6
        """
    )
    fun getEventosByUsuarioIdAndEmpresaId(usuarioId: Long, empresaId: Long): List<EventoConUsuarioDTO>

    /**
     * Cuenta eventos de un usuario/cliente en una empresa
     */
    @Query(
        """
        SELECT COUNT(e)
        FROM Evento e
        WHERE e.cliente.id = :usuarioId
        AND e.empresa.id = :empresaId
        AND e.fechaBaja IS NULL
        """
    )
    fun getCantEventosByUsuarioIdAndEmpresaId(usuarioId: Long, empresaId: Long): Int

    // ==================== BÚSQUEDAS POR CÓDIGO ====================

    /**
     * Busca un evento por su código único dentro de una empresa
     * Carga todas las relaciones necesarias
     */
    @EntityGraph(attributePaths = [
        "tipoEvento",
        "encargado",
        "cliente",
        "capacidad",
        "listaExtra",
        "listaEventoExtraVariable",
        "empresa"
    ])
    @Query(
        """
        SELECT e
        FROM Evento e
        WHERE e.codigo = :codigo
        AND e.empresa.id = :empresaId
        AND e.fechaBaja IS NULL
        """
    )
    fun getByCodigoAndEmpresaId(codigo: String, empresaId: Long): Evento

    /**
     * Busca que el código no exista ya en la empresa
     */
    @Query(
        """
        SELECT COUNT(e) > 0 
        FROM Evento e 
        WHERE e.codigo = :codigo 
        AND e.empresa = :empresa 
        AND e.fechaBaja IS NULL
        """
    )
    fun existCodigoInEmpresa(codigo: String, empresa: Empresa): Boolean

    // ==================== ESTADÍSTICAS ====================

    /**
     * Cuenta eventos activos (no eliminados) de una empresa
     * Utilizado para estadísticas y dashboards
     */
    @Query(
        """
        SELECT COUNT(e)
        FROM Evento e
        WHERE e.empresa.id = :id
        AND e.fechaBaja IS NULL
        """
    )
    fun countActivosByEmpresaId(id: Long): Int

    /**
     * Cuenta clientes únicos que tienen eventos en una empresa
     * Utilizado para estadísticas de clientes
     */
    @Query(
        """
        SELECT COUNT(DISTINCT e.cliente)
        FROM Evento e
        WHERE e.empresa.id = :id
        AND e.cliente.fechaBaja IS NULL
        """
    )
    fun countClientesByEmpresaId(id: Long): Int
}
