package com.estonianport.agendaza.dao

import com.estonianport.agendaza.model.Servicio
import org.springframework.data.repository.CrudRepository

interface ServicioDao : CrudRepository<Servicio, Long>