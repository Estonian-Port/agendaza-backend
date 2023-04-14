package com.estonianport.agendaza.repository

import com.estonianport.agendaza.model.Evento
import com.estonianport.agendaza.model.TipoEvento
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.repository.CrudRepository

interface TipoEventoRepository : CrudRepository<TipoEvento, Long>{
    //@EntityGraph(attributePaths = ["capacidad", "empresa"])
    override fun findAll() : List<TipoEvento>

}