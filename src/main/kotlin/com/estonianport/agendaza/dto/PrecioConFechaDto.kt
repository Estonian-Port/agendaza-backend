package com.estonianport.agendaza.dto

import java.time.LocalDateTime

class PrecioConFechaDto(val id : Long, val desde : LocalDateTime, val hasta : LocalDateTime, val precio : Double,
                        val empresaId : Long, val itemId : Long) {
    
}