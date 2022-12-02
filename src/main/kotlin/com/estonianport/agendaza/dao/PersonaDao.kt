package com.estonianport.agendaza.dao

import com.estonianport.agendaza.model.Persona
import org.springframework.data.repository.CrudRepository

interface PersonaDao : CrudRepository<Persona, Long>