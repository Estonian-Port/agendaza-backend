package com.estonianport.agendaza.repository

import com.estonianport.agendaza.model.Capacidad
import org.springframework.data.repository.CrudRepository

interface CapacidadRepository : CrudRepository<Capacidad, Long>