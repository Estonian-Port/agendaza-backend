package com.estonianport.agendaza.dao

import com.estonianport.agendaza.model.Capacidad
import org.springframework.data.repository.CrudRepository

interface CapacidadDao : CrudRepository<Capacidad, Long>