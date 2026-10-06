package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.Gasto
import com.estonianport.agendaza.model.enums.MedioDePago
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
    val usuarioId: Long,
    val medioDePago: MedioDePago? = null,
    val nombreEvento: String? = null,   // solo respuesta (lista / edición)
    val codigoEvento: String? = null    // entrada opcional al guardar; salida al editar
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
        usuarioId = encargado.id,
        medioDePago = medioDePago,
        nombreEvento = evento?.nombre,
        codigoEvento = evento?.codigo
    )
}