package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.Capacidad
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

data class EventoUsuarioDTO(
    var id: Long,
    var nombre: String,
    var codigo: String,
    var usuario: UsuarioAbmDTO
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
    var capacidad: Capacidad,
    var codigo: String,
    val inicio: LocalDateTime,
    var fin: LocalDateTime,
    val tipoEventoId: Long,
    val empresaId: Long,
    val extraOtro: Double,
    val descuento: Long,
    val listaExtra: List<ExtraDTO>,
    val listaExtraVariable: List<EventoExtraVariableDTO>,
    val cateringOtro: Double,
    val cateringOtroDescripcion: String,
    val listaExtraTipoCatering: List<ExtraDTO>,
    val listaExtraCateringVariable: List<EventoExtraVariableDTO>,
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
    val tipoEventoNombre: String,
    val capacidad: Capacidad,
    val extraOtro: Double,
    val descuento: Long,
    val listaExtra: List<ExtraDTO>,
    val listaExtraVariable: List<EventoExtraVariableDTO>,
    val cateringOtro: Double,
    val cateringOtroDescription: String,
    val listaExtraTipoCatering: List<ExtraDTO>,
    val listaExtraCateringVariable: List<EventoExtraVariableDTO>,
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
    val descuento: Long,
    val listaExtra: List<ExtraDTO>,
    val listaExtraVariable: List<EventoExtraVariableDTO>,
    val tipoEventoExtra: TipoEventoPrecioDTO,
    val fechaEvento: LocalDateTime
) : Serializable

data class EventoCateringDTO(
    var id: Long,
    val nombre: String,
    val codigo: String,
    val cateringOtro: Double,
    val cateringOtroDescripcion: String,
    val listaExtraTipoCatering: List<ExtraDTO>,
    val listaExtraCateringVariable: List<EventoExtraVariableDTO>,
    val tipoEventoId: Long,
    val fechaEvento: LocalDateTime,
    val capacidad: Capacidad
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

// ==================== DTOs para Búsqueda ====================

data class EventoBuscarFechaDTO(
    val empresaId: Long,
    val desde: LocalDateTime,
    val hasta: LocalDateTime
) : Serializable

// ==================== DTOs para Ediciones Parciales (PATCH) ====================

/**
 * DTO para editar la capacidad de un evento (PATCH /eventos/{id}/capacidad)
 */
data class EventoCapacidadDTO(
    val capacidadAdultos: Int,
    val capacidadNinos: Int
) : Serializable

/**
 * DTO para editar el nombre de un evento (PATCH /eventos/{id}/nombre)
 * Nota: Se envía como String simple en el body del request
 */

/**
 * DTO para editar anotaciones (PATCH /eventos/{id}/anotaciones)
 * Nota: Se envía como String simple en el body del request
 */

/**
 * DTO para respuesta de edición de horarios
 */
data class EventoHoraActualizadoDTO(
    val eventoId: Long,
    val inicio: LocalDateTime,
    val fin: LocalDateTime,
    val mensaje: String = "Horarios actualizados correctamente"
) : Serializable

// ==================== Extensiones para Conversión ====================

fun EventoConUsuarioDTO.toEventoUsuarioDto(): EventoUsuarioDTO {
    return EventoUsuarioDTO(
        id = id,
        nombre = nombre,
        codigo = codigo,
        usuario = UsuarioAbmDTO(
            id = usuarioId,
            nombre = usuarioNombre,
            apellido = usuarioApellido,
            username = usuarioUsername
        )
    )
}



fun Evento.toEventoVerDto(listaExtraEvento : List<ExtraDTO>,
                   listaExtraVariableEvento : List<EventoExtraVariableDTO>,
                   listaExtraCatering : List<ExtraDTO>,
                   listaExtraVariableCatering : List<EventoExtraVariableDTO>) : EventoVerDTO{
    return EventoVerDTO(id, nombre, codigo, inicio, fin, tipoEvento.nombre, capacidad, extraOtro,
        descuento, listaExtraEvento, listaExtraVariableEvento, cateringOtro, cateringOtroDescripcion,
        listaExtraCatering, listaExtraVariableCatering, encargado.toUsuarioAbmDto(), cliente.toUsuarioResponseDto(), this.getPresupuestoTotal(), estado, anotaciones)
}

fun Evento.toEventoReservaDto(
    listaExtraEvento: List<ExtraDTO>,
    listaExtraVariableEvento: List<EventoExtraVariableDTO>,
    listaExtraCatering: List<ExtraDTO>,
    listaExtraVariableCatering: List<EventoExtraVariableDTO>
): EventoReservaDTO {
    return EventoReservaDTO(id, nombre, capacidad, codigo, inicio, fin, tipoEvento.id,
        empresa.id, extraOtro, descuento, listaExtraEvento, listaExtraVariableEvento,
        cateringOtro, cateringOtroDescripcion, listaExtraCatering, listaExtraVariableCatering,
        cliente, encargado.id, estado, anotaciones
    )
}

fun Evento.toEventoHoraDto(): EventoHoraDTO {
    return EventoHoraDTO(id, nombre, codigo, inicio, fin)
}

fun Evento.toEventoCateringDto(listaExtra: List<ExtraDTO>,
                        listaExtraVariable: List<EventoExtraVariableDTO>): EventoCateringDTO {
    return EventoCateringDTO(id, nombre, codigo, cateringOtro, cateringOtroDescripcion, listaExtra,
        listaExtraVariable, tipoEvento.id, inicio, capacidad)
}

fun Evento.toEventoExtraDto(listaExtra: List<ExtraDTO>,
                     listaExtraVariable: List<EventoExtraVariableDTO>): EventoExtraDTO {
    return EventoExtraDTO(id, nombre, codigo, extraOtro, descuento, listaExtra,
        listaExtraVariable, empresa.toTipoEventoPrecioDTO(inicio, tipoEvento), inicio)
}