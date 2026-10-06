package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.Pago
import com.estonianport.agendaza.model.enums.Concepto
import com.estonianport.agendaza.model.enums.MedioDePago
import java.io.Serializable
import java.time.LocalDateTime

data class PagoDTO(
    val id: Long, val monto: Double, val concepto: Concepto?, val numeroCuota: String?, val codigo: String,
    val medioDePago: MedioDePago?, val nombreEvento: String, val fechaEvento: LocalDateTime, val fecha : LocalDateTime,
    val empresaId: Long = 0, val usuarioId: Long = 0): Serializable

data class ResumenPagosMesDTO(
    val ingresos: Double,
    val egresos: Double,
    val balance: Double,
    val cantidadIngresos: Long,
    val cantidadEgresos: Long)

data class TotalesPagosMes(
    val total: Double,
    val cantidad: Long)


fun Pago.toDTO(): PagoDTO {
    return PagoDTO(
        id = id,
        monto = monto,
        codigo = evento.codigo,
        medioDePago = medioDePago,
        fechaEvento = evento.inicio,
        nombreEvento = evento.nombre,
        concepto = concepto,
        numeroCuota = numeroCuota,
        empresaId = evento.empresa.id,
        usuarioId = encargado.id,
        fecha = fecha
    )
}