package com.estonianport.agendaza.repository

import com.estonianport.agendaza.model.TipoEvento
import org.springframework.data.repository.CrudRepository

interface TipoEventoRepository : CrudRepository<TipoEvento, Long>