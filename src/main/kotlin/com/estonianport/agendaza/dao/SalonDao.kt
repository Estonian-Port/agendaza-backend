package com.estonianport.agendaza.repository

import com.estonianport.agendaza.model.Salon
import org.springframework.data.repository.CrudRepository

interface SalonDao : CrudRepository<Salon, Long>