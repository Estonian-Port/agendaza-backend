package com.estonianport.agendaza.dto

import java.io.Serializable
import java.time.LocalDateTime

data class PrecioConFechaDTO(val id : Long, val desde : LocalDateTime, val hasta : LocalDateTime, val precio : Double,
                        val empresaId : Long, val itemId : Long): Serializable