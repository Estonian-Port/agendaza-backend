package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.enums.TipoGasto
import java.time.LocalDateTime
import java.time.YearMonth

/** Proyección liviana de un evento para contar eventos y personas. */
class EventoBalanceDTO(
    val inicio: LocalDateTime,
    val capacidadAdultos: Int,
    val capacidadNinos: Int
)

/** Totales de un mes calendario. */
data class BalanceMes(
    val periodo: YearMonth,
    val ingresos: Double,
    val gastosEvento: Double,
    /** Todo gasto que no es de tipo EVENTO (incluye sueldos). */
    val gastosGenerales: Double,
    /** Subconjunto de gastosGenerales: solo tipo SUELDOS. */
    val sueldos: Double,
    /** Cotización del dólar del día 1 del mes (null si no se pudo obtener). */
    val cotizacion: Double?,
    val eventos: Int,
    val personas: Int
) {
    val gastos: Double get() = gastosEvento + gastosGenerales
    val resultado: Double get() = ingresos - gastos

    fun usd(montoArs: Double): Double? = cotizacion?.let { montoArs / it }
}

/** Todo lo que necesita el PDF, ya calculado. */
data class BalanceReporte(
    val empresaNombre: String,
    val logoUrl: String?,
    val desde: YearMonth,
    val hasta: YearMonth,
    val meses: List<BalanceMes>,
    val gastosPorTipo: Map<TipoGasto, Double>,
    val casaDolar: String
) {
    /** Solo se muestran dólares si hay cotización para TODOS los meses. */
    val usdDisponible: Boolean get() = meses.isNotEmpty() && meses.all { it.cotizacion != null }

    /** Suma de los valores mes a mes convertidos con la cotización de cada mes. */
    fun usdTotal(f: (BalanceMes) -> Double): Double? =
        if (usdDisponible) meses.sumOf { m -> m.usd(f(m)) ?: 0.0 } else null
}