package com.estonianport.agendaza.dao

import com.estonianport.agendaza.model.EventoExtraVariable
import org.springframework.data.repository.CrudRepository

interface ExtraVariableDao: CrudRepository<EventoExtraVariable, Long>