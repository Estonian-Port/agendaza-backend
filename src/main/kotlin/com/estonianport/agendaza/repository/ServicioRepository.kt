package com.estonianport.agendaza.repository

import com.estonianport.agendaza.dto.ServicioDTO
import com.estonianport.agendaza.model.Pago
import com.estonianport.agendaza.model.Servicio
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface ServicioRepository : CrudRepository<Servicio, Long> {

    @Query("SELECT new com.estonianport.agendaza.dto.ServicioDTO(s.id, s.nombre) FROM Servicio s " +
            "INNER JOIN s.listaEmpresa e WHERE e.id = :empresaId AND s.fechaBaja IS NULL ORDER BY s.id DESC")
    fun getAllServicioByEmpresaId(empresaId: Long, pageable: Pageable): Page<ServicioDTO>

    @Query("SELECT COUNT(s) FROM Servicio s INNER JOIN s.listaEmpresa e WHERE e.id = :empresaId " +
            "AND s.fechaBaja IS NULL")
    fun getCantidadServicio(empresaId: Long): Int

    @Query("SELECT new com.estonianport.agendaza.dto.ServicioDTO(s.id, s.nombre) FROM Servicio s " +
            "INNER JOIN s.listaEmpresa e WHERE e.id = :empresaId AND s.nombre ILIKE %:buscar% AND s.fechaBaja IS NULL ORDER BY s.id DESC")
    fun getAllServicioFilterNombre(empresaId: Long, buscar: String, pageable: Pageable): Page<ServicioDTO>

    @Query("SELECT COUNT(s) FROM Servicio s INNER JOIN s.listaEmpresa e WHERE e.id = :empresaId " +
            "AND s.nombre ILIKE %:buscar% AND s.fechaBaja IS NULL")
    fun getCantidadServicioFiltrados(empresaId: Long, buscar: String): Int

    @Query("""SELECT new com.estonianport.agendaza.dto.ServicioDTO(s.id, s.nombre)
        FROM Servicio s
        LEFT JOIN s.listaEmpresa e WITH e.id = :empresaId
        WHERE e.id IS NULL AND s.fechaBaja IS NULL
        ORDER BY s.id DESC
        LIMIT 10""")
    fun getAllServicioAgregar(empresaId: Long): List<ServicioDTO>

    @Query("""SELECT new com.estonianport.agendaza.dto.ServicioDTO(s.id, s.nombre)
        FROM Servicio s
        LEFT JOIN s.listaTipoEvento te
        WHERE te.id = :tipoEventoId
          AND s.fechaBaja IS NULL
        ORDER BY s.id DESC""")
    fun getAllServicioByTipoEventoId(tipoEventoId: Long): List<ServicioDTO>

}