package com.estonianport.agendaza.dao

import com.estonianport.agendaza.model.Cargo
import org.springframework.data.repository.CrudRepository

interface CargoDao : CrudRepository<Cargo, Long>