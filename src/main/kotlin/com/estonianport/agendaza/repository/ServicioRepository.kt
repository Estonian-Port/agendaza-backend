package com.estonianport.agendaza.repository

import com.estonianport.agendaza.model.Servicio
import org.springframework.data.repository.CrudRepository

interface ServicioRepository : CrudRepository<Servicio, Long>