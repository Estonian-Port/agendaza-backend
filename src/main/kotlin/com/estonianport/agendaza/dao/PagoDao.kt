package com.estonianport.agendaza.dao

import com.estonianport.agendaza.model.Pago
import org.springframework.data.repository.CrudRepository

interface PagoDao : CrudRepository<Pago, Long>