package com.estonianport.agendaza.repository

import com.estonianport.agendaza.model.Cargo
import org.springframework.data.repository.CrudRepository

interface CargoRepository : CrudRepository<Cargo, Long>