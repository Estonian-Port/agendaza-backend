package com.estonianport.agendaza.repository

import com.estonianport.agendaza.model.Pago
import org.springframework.data.repository.CrudRepository

interface PagoRepository : CrudRepository<Pago, Long>