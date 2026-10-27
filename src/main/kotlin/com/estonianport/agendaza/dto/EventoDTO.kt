package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.Evento
import com.estonianport.agendaza.model.enums.Estado
import com.estonianport.agendaza.model.Usuario
import java.io.Serializable
import java.time.LocalDateTime

// ==================== DTOs Principales ====================

data class EventoDTO(
    var id: Long,
    var nombre: String,
    var codigo: String,
    var inicio: LocalDateTime,
    var fin: LocalDateTime,
    var tipoEvento: String
) : Serializable

data class EventoAgendaDTO(
    var id: Long,
    var title: String,
    var start: LocalDateTime,
    var end: LocalDateTime
) : Serializable

// ==================== DTOs para Creación ====================

data class EventoReservaDTO(
    val id: Long,
    val nombre: String,
    var capacidadAdultos: Int,
    val capacidadNinos: Int,
    var codigo: String,
    val inicio: LocalDateTime,
    var fin: LocalDateTime,
    val tipoEventoId: Long,
    val empresaId: Long,
    val extraOtro: Double,
    val extraOtroDescripcion: String,
    val precioTipoEvento: Double = 0.0,
    val listaExtra: List<ExtraDTO>,
    val cateringOtro: Double,
    val cateringOtroDescripcion: String,
    val cliente: Usuario,
    val encargadoId: Long,
    val estado: Estado,
    val anotaciones: String
) : Serializable

// ==================== DTOs para Lectura ====================

data class EventoVerDTO(
    val id: Long,
    val nombre: String,
    val codigo: String,
    val inicio: LocalDateTime,
    val fin: LocalDateTime,
    val tipoEventoId: Long,
    val tipoEventoNombre: String,
    val capacidadAdultos: Int,
    val capacidadNinos: Int,
    val extraOtro: Double,
    val extraOtroDescripcion: String,
    val precioTipoEvento: Double,
    val listaExtra: List<ExtraDTO>,
    val cateringOtro: Double,
    val cateringOtroDescripcion: String,
    val encargado: UsuarioAbmDTO,
    val cliente: UsuarioResponseDto,
    val presupuesto: Double,
    val estado: Estado,
    val anotaciones: String
) : Serializable

data class EventoPagoDTO(
    val id: Long,
    val nombre: String,
    val codigo: String,
    var precioTotal: Double
) : Serializable

// ==================== DTOs para Consultas Específicas ====================

data class EventoExtraDTO(
    var id: Long,
    val nombre: String,
    val codigo: String,
    val extraOtro: Double,
    val extraOtroDescripcion: String,
    val listaEventoExtra: List<ExtraDTO>,
    val tipoEventoExtra: TipoEventoPrecioDTO,
    val fechaEvento: LocalDateTime
) : Serializable

data class EventoCateringDTO(
    var id: Long,
    val nombre: String,
    val codigo: String,
    val cateringOtro: Double,
    val cateringOtroDescripcion: String,
    val listaExtra: List<ExtraDTO>,
    val tipoEventoId: Long,
    val fechaEvento: LocalDateTime,
    val capacidadAdultos: Int,
    val capacidadNinos: Int
) : Serializable

data class EventoHoraDTO(
    var id: Long,
    val nombre: String,
    val codigo: String,
    val inicio: LocalDateTime,
    val fin: LocalDateTime
) : Serializable

// ==================== DTOs para Usuario ====================

data class EventoConUsuarioDTO(
    val id: Long,
    val nombre: String,
    val codigo: String,
    val usuarioId: Long,
    val usuarioNombre: String,
    val usuarioApellido: String,
    val usuarioUsername: String?
) : Serializable

// ==================== DTOs para Ediciones Parciales (PATCH) ====================

/**
 * DTO para editar la capacidad de un evento (PATCH /eventos/{id}/capacidad)
 */
data class EventoPreciosDTO(
    val precioTipoEvento: Double,
    val listaExtra: List<ExtraDTO>,
    val listaExtraVariable: List<EventoExtraVariableDTO>,
    val capacidadAdultos: Int
) : Serializable

data class EventoCapacidadDTO(
    val capacidadAdultos: Int,
    val capacidadNinos: Int
) : Serializable

// ==================== Extensiones para Conversión ====================

fun Evento.toEventoVerDto(listaExtraEvento : List<ExtraDTO>) : EventoVerDTO{
    return EventoVerDTO(id, nombre, codigo, inicio, fin, tipoEvento.id, tipoEvento.nombre, capacidadAdultos, capacidadNinos, extraOtro,
        extraOtroDescripcion, precioTipoEvento, listaExtraEvento, cateringOtro, cateringOtroDescripcion, encargado.toUsuarioAbmDto(),
        cliente.toUsuarioResponseDto(), this.getPresupuestoTotal(), estado, anotaciones)
}

fun Evento.toEventoReservaDto(
    listaExtraEvento: List<ExtraDTO>
): EventoReservaDTO {
    return EventoReservaDTO(id, nombre, capacidadAdultos, capacidadNinos, codigo, inicio, fin, tipoEvento.id,
        empresa.id, extraOtro, extraOtroDescripcion, precioTipoEvento, listaExtraEvento,
        cateringOtro, cateringOtroDescripcion, cliente, encargado.id, estado, anotaciones
    )
}

fun Evento.toEventoHoraDto(): EventoHoraDTO {
    return EventoHoraDTO(id, nombre, codigo, inicio, fin)
}

fun Evento.toEventoCateringDto(listaExtra: List<ExtraDTO>): EventoCateringDTO {
    return EventoCateringDTO(id, nombre, codigo, cateringOtro, cateringOtroDescripcion, listaExtra,
        tipoEvento.id, inicio, capacidadAdultos, capacidadNinos)
}

fun Evento.toEventoExtraDto(listaExtra: List<ExtraDTO>): EventoExtraDTO {
    return EventoExtraDTO(id, nombre, codigo, extraOtro, extraOtroDescripcion, listaExtra,
        TipoEventoPrecioDTO(tipoEvento.id, tipoEvento.nombre, precioTipoEvento), inicio)
}
