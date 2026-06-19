package com.estonianport.agendaza.common

import java.time.LocalDateTime

/**
 * Extensión para obtener el último segundo del mes actual.
 */
fun LocalDateTime.toEndOfMonth(): LocalDateTime {
    return this.plusMonths(1).minusSeconds(1)
}