package com.estonianport.agendaza.repository

import com.estonianport.agendaza.model.EventoExtraVariable
import org.springframework.data.repository.CrudRepository

interface ExtraVariableRepository: CrudRepository<EventoExtraVariable, Long>