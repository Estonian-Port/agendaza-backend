package com.estonianport.agendaza.dao

import com.estonianport.agendaza.model.PrecioConFecha
import com.estonianport.agendaza.model.PrecioConFechaTipoEvento
import org.springframework.data.repository.CrudRepository

interface PrecioConFechaTipoEventoDao : CrudRepository<PrecioConFechaTipoEvento, Long>