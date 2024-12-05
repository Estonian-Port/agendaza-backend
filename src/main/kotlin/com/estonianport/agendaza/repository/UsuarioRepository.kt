package com.estonianport.agendaza.repository

import com.estonianport.agendaza.dto.EmpresaDTO
import com.estonianport.agendaza.dto.UsuarioAbmDTO
import com.estonianport.agendaza.dto.UsuarioEditCargoDTO
import com.estonianport.agendaza.dto.UsuarioPerfilDTO
import com.estonianport.agendaza.model.Usuario
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UsuarioRepository : CrudRepository<Usuario, Long> {


    fun getByUsername(username: String): Usuario

    @Query("SELECT new com.estonianport.agendaza.dto.UsuarioAbmDTO(c.usuario.id, c.usuario.nombre, " +
            "c.usuario.apellido, c.usuario.username) FROM Cargo c WHERE c.empresa.id = :empresaId AND " +
            "c.fechaBaja IS NULL")
    fun getAllUsuario(empresaId: Long, pageable: PageRequest) : Page<UsuarioAbmDTO>

    @Query("SELECT new com.estonianport.agendaza.dto.UsuarioAbmDTO(c.usuario.id, c.usuario.nombre, " +
            "c.usuario.apellido, c.usuario.username) FROM Cargo c WHERE c.empresa.id = :empresaId " +
            "AND (c.usuario.nombre ILIKE %:buscar% OR c.usuario.apellido ILIKE %:buscar%) AND c.fechaBaja IS NULL")
    fun getAllUsuarioFiltrados(empresaId : Long, buscar : String, pageable : Pageable) : Page<UsuarioAbmDTO>

    @Query("SELECT COUNT(c) FROM Cargo c WHERE c.empresa.id = :empresaId AND c.fechaBaja IS NULL")
    fun getCantidadUsuario(empresaId : Long) : Int

    @Query("SELECT COUNT(c) FROM Cargo c WHERE c.empresa.id = :empresaId AND " +
            "(c.usuario.nombre ILIKE %:buscar% OR c.usuario.apellido ILIKE %:buscar%) AND c.fechaBaja IS NULL")
    fun getCantidadUsuarioFiltrados(empresaId : Long, buscar: String) : Int

    @Query("SELECT new com.estonianport.agendaza.dto.UsuarioAbmDTO(c.id, c.nombre, " +
            "c.apellido, c.username) FROM Evento ev INNER JOIN ev.empresa em INNER JOIN ev.cliente c " +
            "WHERE ev.empresa.id = :empresaId AND c.fechaBaja IS NULL")
    fun getAllCliente(empresaId: Long, pageable: PageRequest) : Page<UsuarioAbmDTO>

    @Query("SELECT new com.estonianport.agendaza.dto.UsuarioAbmDTO(c.id, c.nombre, " +
            "c.apellido, c.username) FROM Evento ev INNER JOIN ev.empresa em INNER JOIN ev.cliente c " +
            "WHERE ev.empresa.id = :empresaId AND (c.nombre ILIKE %:buscar% OR " +
            "c.apellido ILIKE %:buscar%) AND c.fechaBaja IS NULL")
    fun getAllClienteFiltrados(empresaId : Long, buscar : String, pageable : Pageable) : Page<UsuarioAbmDTO>

    @Query("SELECT COUNT(c) FROM Evento ev INNER JOIN ev.empresa em INNER JOIN ev.cliente c " +
            "WHERE ev.empresa.id = :empresaId AND c.fechaBaja IS NULL")
    fun getCantidadCliente(empresaId : Long) : Int

    @Query("SELECT COUNT(c) FROM Evento ev INNER JOIN ev.empresa em INNER JOIN ev.cliente c " +
            "WHERE ev.empresa.id = :empresaId AND (c.nombre ILIKE %:buscar% " +
            "OR c.apellido ILIKE %:buscar%) AND c.fechaBaja IS NULL")
    fun getCantidadClienteFiltrados(empresaId : Long, buscar: String) : Int

    fun findOneByUsername(username: String): Usuario?

    fun getUsuarioByEmail(email : String): Usuario?

    fun getUsuarioByCelular(celular : Long) : Usuario?

    //@EntityGraph(attributePaths = ["listaCargo"])
    override fun findById(id: Long) : Optional<Usuario>


    @Query("SELECT new com.estonianport.agendaza.dto.UsuarioEditCargoDTO(" +
            "c.usuario.id, c.empresa.id, c.tipoCargo) FROM Cargo c WHERE c.empresa.id = :empresaId " +
            "AND c.usuario.id = :usuarioId AND c.fechaBaja IS NULL")
    fun getUsuarioOfEmpresa(usuarioId: Long, empresaId: Long): UsuarioEditCargoDTO

    @Query("SELECT new com.estonianport.agendaza.dto.UsuarioPerfilDTO(u.id, u.nombre, u.apellido, " +
            "u.username, u.email, u.celular, u.fechaNacimiento) FROM Usuario u WHERE u.id = :usuarioId ")
    fun getUsuarioPerfil(usuarioId: Long): UsuarioPerfilDTO

    @Query("SELECT new com.estonianport.agendaza.dto.EmpresaDTO" +
            "(e.id, e.nombre, c.tipoCargo, e.email, e.telefono, e.calle, e.numero, e.municipio) " +
            "FROM Usuario u INNER JOIN u.listaCargo c INNER JOIN c.empresa e WHERE u.id = :usuarioId " +
            "AND c.fechaBaja IS NULL")
    fun getAllEmpresaByUsuarioId(usuarioId: Long): List<EmpresaDTO>
}