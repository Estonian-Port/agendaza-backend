package com.estonianport.agendaza.common

import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

/**
 * Extensión para obtener el último segundo del mes actual.
 */
fun LocalDateTime.toEndOfMonth(): LocalDateTime {
    return with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.of(23, 59, 59))
}
