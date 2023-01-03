package com.estonianport.agendaza.dao

import com.estonianport.agendaza.model.Evento
import org.springframework.data.repository.CrudRepository

interface EventoDao : CrudRepository<Evento, Long>