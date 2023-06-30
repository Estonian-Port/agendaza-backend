package com.estonianport.agendaza.repository

import com.estonianport.agendaza.model.Cargo
import com.estonianport.agendaza.model.Evento
import com.estonianport.agendaza.model.Usuario
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.repository.CrudRepository
import org.yaml.snakeyaml.events.Event.ID
import java.util.*

interface CargoRepository : CrudRepository<Cargo, Long>{

    //@EntityGraph(attributePaths = ["usuario", "empresa"])
    override fun findAll() : List<Cargo>

    //@EntityGraph(attributePaths = ["usuario", "empresa"])
    override fun findById(id : Long) : Optional<Cargo>

    //@EntityGraph(attributePaths = ["usuario", "empresa"])
    fun findAllByUsuario(usuario : Usuario): List<Cargo>
}

