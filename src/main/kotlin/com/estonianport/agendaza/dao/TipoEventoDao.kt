package com.estonianport.agendaza.dao

import com.estonianport.agendaza.model.TipoEvento
import org.springframework.data.repository.CrudRepository

interface TipoEventoDao : CrudRepository<TipoEvento, Long>