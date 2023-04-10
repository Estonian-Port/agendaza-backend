package com.estonianport.agendaza.repository

import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Evento
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime

interface EventoRepository : CrudRepository<Evento, Long>{

    fun findAllByInicioBetweenAndListaEmpresa(inicio: LocalDateTime, fin: LocalDateTime, empresa: Empresa): List<Evento>

}