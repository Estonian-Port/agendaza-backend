package com.estonianport.agendaza.repository

import com.estonianport.agendaza.dto.AgendaDTO
import com.estonianport.agendaza.model.Cargo
import com.estonianport.agendaza.model.TipoCargo
import com.estonianport.agendaza.model.Usuario
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

interface CargoRepository : CrudRepository<Cargo, Long>{

    override fun findAll() : List<Cargo>

    //@EntityGraph(attributePaths = ["usuario", "empresa"])
    override fun findById(id : Long) : Optional<Cargo>

    //@EntityGraph(attributePaths = ["usuario", "empresa"])
    fun findAllByUsuario(usuario : Usuario): List<Cargo>

    @Query("SELECT new com.estonianport.agendaza.dto.AgendaDTO(c.empresa.id, c.empresa.nombre, c.tipoCargo) FROM Cargo c WHERE c.usuario.id = ?1 AND c.fechaBaja IS NULL")
    fun getListaCargosByUsuarioId(usuarioId : Long) : List<AgendaDTO>

    @Query("SELECT c FROM Cargo c WHERE c.empresa.id = :empresaId AND c.usuario.id = :usuarioId AND c.fechaBaja IS NULL")
    fun getCargoByEmpresaIdAndUsuarioId(empresaId: Long, usuarioId: Long): Cargo

    @Query("SELECT c.tipoCargo FROM Cargo c WHERE c.empresa.id = :empresaId AND c.usuario.id = :usuarioId AND c.fechaBaja IS NULL")
    fun getTipoCargoByEmpresaIdAndUsuarioId(empresaId: Long, usuarioId: Long): TipoCargo

}

