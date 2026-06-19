package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.enums.Concepto
import com.estonianport.agendaza.model.enums.MedioDePago
import java.io.Serializable
import java.time.LocalDateTime

data class PagoDTO(
    val id: Long, val monto: Double, val concepto: Concepto?, val numeroCuota: String?, val codigo: String,
    val medioDePago: MedioDePago?, val nombreEvento: String, val fechaEvento: LocalDateTime, val fecha : LocalDateTime,
    val empresaId: Long = 0, val usuarioId: Long = 0): Serializable