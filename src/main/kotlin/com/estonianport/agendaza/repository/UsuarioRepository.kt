package com.estonianport.agendaza.repository

import com.estonianport.agendaza.dto.*
import com.estonianport.agendaza.model.Usuario
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UsuarioRepository : CrudRepository<Usuario, Long> {

    // ==================== BÚSQUEDAS BÁSICAS ====================

    @Query("SELECT u FROM Usuario u WHERE u.username = :username")
    fun getByUsername(username: String): Usuario?

    @Query("SELECT u FROM Usuario u WHERE u.email = :email AND u.fechaBaja IS NULL")
    fun getByEmail(email: String): Usuario?

    @Query("SELECT u FROM Usuario u WHERE u.celular = :celular AND u.fechaBaja IS NULL")
    fun getByCelular(celular: Long): Usuario?

    // ==================== DTOs PARA RESPUESTAS ====================

    @Query("""
        SELECT new com.estonianport.agendaza.dto.UsuarioResponseDto(
            u.id, u.nombre, u.apellido, u.username, u.email, u.celular
        ) 
        FROM Usuario u 
        WHERE u.email = :email
    """)
    fun getUsuarioDtoByEmail(email: String): UsuarioResponseDto?

    @Query("""
        SELECT new com.estonianport.agendaza.dto.UsuarioPerfilDTO(
            u.id, u.nombre, u.apellido, u.username, u.email, u.celular, u.fechaNacimiento
        ) 
        FROM Usuario u 
        WHERE u.id = :usuarioId
    """)
    fun getUsuarioPerfil(usuarioId: Long): UsuarioPerfilDTO?

    @Query("""
        SELECT new com.estonianport.agendaza.dto.UsuarioEditCargoDTO(
            c.usuario.id, c.empresa.id, c.tipoCargo
        ) 
        FROM Cargo c 
        WHERE c.empresa.id = :empresaId 
        AND c.usuario.id = :usuarioId 
        AND c.fechaBaja IS NULL
    """)
    fun getUsuarioOfEmpresa(usuarioId: Long, empresaId: Long): UsuarioEditCargoDTO?

    // ==================== EMPLEADOS (USUARIOS CON CARGO) ====================

    @Query("""
        SELECT new com.estonianport.agendaza.dto.UsuarioAbmDTO(
            c.usuario.id, c.usuario.nombre, c.usuario.apellido, c.usuario.username
        ) 
        FROM Cargo c 
        WHERE c.empresa.id = :empresaId 
        AND c.fechaBaja IS NULL
        ORDER BY c.usuario.apellido, c.usuario.nombre
    """)
    fun getAllUsuario(empresaId: Long, pageable: Pageable): Page<UsuarioAbmDTO>

    @Query("""
        SELECT new com.estonianport.agendaza.dto.UsuarioAbmDTO(
            c.usuario.id, c.usuario.nombre, c.usuario.apellido, c.usuario.username
        ) 
        FROM Cargo c 
        WHERE c.empresa.id = :empresaId 
        AND (LOWER(c.usuario.nombre) LIKE LOWER(CONCAT('%', :buscar, '%')) 
             OR LOWER(c.usuario.apellido) LIKE LOWER(CONCAT('%', :buscar, '%'))
             OR LOWER(c.usuario.username) LIKE LOWER(CONCAT('%', :buscar, '%')))
        AND c.fechaBaja IS NULL
        ORDER BY c.usuario.apellido, c.usuario.nombre
    """)
    fun getAllUsuarioFiltrados(empresaId: Long, buscar: String, pageable: Pageable): Page<UsuarioAbmDTO>

    @Query("""
        SELECT COUNT(c) 
        FROM Cargo c 
        WHERE c.empresa.id = :empresaId 
        AND c.fechaBaja IS NULL
    """)
    fun getCantidadUsuario(empresaId: Long): Int

    @Query("""
        SELECT COUNT(c) 
        FROM Cargo c 
        WHERE c.empresa.id = :empresaId 
        AND (LOWER(c.usuario.nombre) LIKE LOWER(CONCAT('%', :buscar, '%')) 
             OR LOWER(c.usuario.apellido) LIKE LOWER(CONCAT('%', :buscar, '%'))
             OR LOWER(c.usuario.username) LIKE LOWER(CONCAT('%', :buscar, '%')))
        AND c.fechaBaja IS NULL
    """)
    fun getCantidadFiltrados(empresaId: Long, buscar: String): Int

    // ==================== CLIENTES ====================

    @Query("""
        SELECT DISTINCT new com.estonianport.agendaza.dto.UsuarioAbmDTO(
            c.id, c.nombre, c.apellido, c.username
        ) 
        FROM Evento ev 
        INNER JOIN ev.cliente c 
        WHERE ev.empresa.id = :empresaId 
        AND c.fechaBaja IS NULL
        ORDER BY c.apellido, c.nombre
    """)
    fun getAllCliente(empresaId: Long, pageable: Pageable): Page<UsuarioAbmDTO>
    @Query("""
        SELECT DISTINCT new com.estonianport.agendaza.dto.UsuarioAbmDTO(
            c.id, c.nombre, c.apellido, c.username
        ) 
        FROM Evento ev 
        INNER JOIN ev.cliente c 
        WHERE ev.empresa.id = :empresaId 
        AND (LOWER(c.nombre) LIKE LOWER(CONCAT('%', :buscar, '%')) 
             OR LOWER(c.apellido) LIKE LOWER(CONCAT('%', :buscar, '%')))
        AND c.fechaBaja IS NULL
        ORDER BY c.apellido, c.nombre
    """)
    fun getAllClienteFiltrados(empresaId: Long, buscar: String, pageable: Pageable): Page<UsuarioAbmDTO>

    @Query("""
        SELECT COUNT(DISTINCT c) 
        FROM Evento ev 
        INNER JOIN ev.cliente c 
        WHERE ev.empresa.id = :empresaId 
        AND c.fechaBaja IS NULL
    """)
    fun getCantidadCliente(empresaId: Long): Int

    @Query("""
        SELECT COUNT(DISTINCT c) 
        FROM Evento ev 
        INNER JOIN ev.cliente c 
        WHERE ev.empresa.id = :empresaId 
        AND (LOWER(c.nombre) LIKE LOWER(CONCAT('%', :buscar, '%')) 
             OR LOWER(c.apellido) LIKE LOWER(CONCAT('%', :buscar, '%')))
        AND c.fechaBaja IS NULL
    """)
    fun getCantidadClienteFiltrados(empresaId: Long, buscar: String): Int

    // ==================== EMPRESAS ====================

    @Query("""
        SELECT new com.estonianport.agendaza.dto.EmpresaAbmDTO(
            e.id, e.nombre, c.tipoCargo, e.email, e.telefono, e.calle, e.numero, e.municipio
        ) 
        FROM Usuario u 
        INNER JOIN u.listaCargo c 
        INNER JOIN c.empresa e 
        WHERE u.id = :usuarioId 
        AND c.fechaBaja IS NULL
        ORDER BY e.nombre
    """)
    fun getAllEmpresaByUsuarioId(usuarioId: Long): List<EmpresaAbmDTO>

    // ==================== VALIDACIONES ====================

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Usuario u WHERE u.email = :email")
    fun existsByEmail(email: String): Boolean

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Usuario u WHERE u.celular = :celular")
    fun existsByCelular(celular: Long): Boolean

}
