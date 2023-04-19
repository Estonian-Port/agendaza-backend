package com.estonianport.agendaza.repository

import com.estonianport.agendaza.model.Evento
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.Servicio
import com.estonianport.agendaza.model.TipoEvento
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.repository.CrudRepository

interface TipoEventoRepository : CrudRepository<TipoEvento, Long>{
    //@EntityGraph(attributePaths = ["capacidad", "empresa"])
    override fun findAll() : List<TipoEvento>
    fun getAllByListaExtra(extra : Extra): MutableList<TipoEvento>?
    fun getAllByListaServicio(servicio: Servicio): MutableList<TipoEvento>?

}