package com.estonianport.agendaza.repository

import com.estonianport.agendaza.model.TipoEvento
import com.estonianport.agendaza.model.Usuario
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.yaml.snakeyaml.events.Event.ID
import java.util.*

@Repository
interface UsuarioRepository : CrudRepository<Usuario, Long> {

    //@EntityGraph(attributePaths = ["listaEventosContratados", "listaCargo"])
    override fun findAll() : List<Usuario>

    //@EntityGraph(attributePaths = ["listaCargo"])
    fun getByUsername(username: String): Usuario

    fun findOneByUsername(username: String): Usuario?

    fun getUsuarioByEmail(email : String): Usuario?

    fun getUsuarioByCelular(celular : Long) : Usuario?

    //@EntityGraph(attributePaths = ["listaCargo"])
    override fun findById(id: Long) : Optional<Usuario>
}