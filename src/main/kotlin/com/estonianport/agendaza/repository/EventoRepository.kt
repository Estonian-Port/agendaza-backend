package com.estonianport.agendaza.repository

import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Evento
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime

interface EventoRepository : CrudRepository<Evento, Long>{

    @EntityGraph(attributePaths = ["capacidad", "encargado", "cliente", "tipoEvento"])
    override fun findAll() : List<Evento>

    fun findAllByInicioBetweenAndListaEmpresa(inicio: LocalDateTime, fin: LocalDateTime, empresa: Empresa): List<Evento>

    @EntityGraph(attributePaths = ["capacidad", "encargado", "cliente", "tipoEvento.capacidad"])
    fun findAllByListaEmpresa(empresa: Empresa) : List<Evento>

}