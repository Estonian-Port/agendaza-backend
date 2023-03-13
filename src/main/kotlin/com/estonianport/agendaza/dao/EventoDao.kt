package com.estonianport.agendaza.dao

import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Evento
import com.estonianport.agendaza.model.Salon
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime

interface EventoDao : CrudRepository<Evento, Long>{

    fun findAllByInicioBetweenAndListaEmpresa(inicio: LocalDateTime, fin: LocalDateTime, empresa: Empresa): List<Evento>

}