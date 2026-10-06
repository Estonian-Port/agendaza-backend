package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.Gasto
import com.estonianport.agendaza.model.TipoEvento
import com.estonianport.agendaza.model.enums.TipoGasto
import java.io.Serializable
import java.time.LocalDateTime

data class GastoDTO(
    val id: Long,
    val monto: Double,
    val tipoGasto: TipoGasto,
    val descripcion: String,
    val fecha: LocalDateTime,
    val eventoId: Long?,
    val empresaId: Long,
    val usuarioId: Long
) : Serializable

fun Gasto.toDTO(): GastoDTO {
    return GastoDTO(
        id = id,
        monto = monto,
        tipoGasto = tipoGasto,
        descripcion = descripcion,
        fecha = fecha,
        eventoId = evento?.id,
        empresaId = empresa.id,
        usuarioId = encargado.id
    )
}