package com.estonianport.agendaza.service

import GenericService
import com.estonianport.agendaza.model.Salon

interface SalonService : GenericService<Salon, Long> {
    fun count(): Long?
}