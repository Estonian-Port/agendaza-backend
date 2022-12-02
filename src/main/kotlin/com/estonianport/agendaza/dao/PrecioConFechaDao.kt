package com.estonianport.agendaza.dao

import com.estonianport.agendaza.model.PrecioConFecha
import org.springframework.data.repository.CrudRepository

interface PrecioConFechaDao : CrudRepository<PrecioConFecha, Long>