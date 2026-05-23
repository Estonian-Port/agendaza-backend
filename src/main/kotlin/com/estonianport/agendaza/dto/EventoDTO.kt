package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.Capacidad
import com.estonianport.agendaza.model.enums.Estado
import com.estonianport.agendaza.model.Usuario
import java.time.LocalDateTime

data class EventoDTO(var id: Long, var nombre: String, var codigo : String,
                var inicio : LocalDateTime, var fin : LocalDateTime, var tipoEvento : String) {}

data class EventoUsuarioDTO(var id: Long, var nombre: String, var codigo : String, var usuario: UsuarioAbmDTO)

data class EventoAgendaDTO(var id: Long, var title: String, var start : LocalDateTime, var end : LocalDateTime) {}

data class EventoReservaDTO(val id: Long, val nombre: String, var capacidad : Capacidad, var codigo : String,
                       val inicio : LocalDateTime, var fin : LocalDateTime, val tipoEventoId : Long,
                       val empresaId : Long, val extraOtro : Double, val descuento : Long,
                       val listaExtra : List<ExtraDTO>, val listaExtraVariable : List<EventoExtraVariableDTO>,
                       val cateringOtro : Double, val cateringOtroDescripcion : String,
                       val listaExtraTipoCatering : List<ExtraDTO>,
                       val listaExtraCateringVariable : List<EventoExtraVariableDTO>,
                       val cliente : Usuario, val encargadoId : Long, val estado : Estado, val anotaciones : String) {}

data class EventoPagoDTO(val id : Long, val nombre : String, val codigo : String,
                    var precioTotal : Double) {}

data class EventoExtraDTO(val id: Long, val nombre: String, val codigo: String,
                     val extraOtro: Double, val descuento: Long,
                     val listaExtra: List<ExtraDTO>, val listaExtraVariable: List<EventoExtraVariableDTO>,
                     val tipoEventoExtra: TipoEventoPrecioDTO, val fechaEvento: LocalDateTime)

data class EventoCateringDTO(val id : Long, val nombre : String, val codigo : String, val cateringOtro : Double,
                        val cateringOtroDescripcion : String, val listaExtraTipoCatering : List<ExtraDTO>,
                        val listaExtraCateringVariable : List<EventoExtraVariableDTO>,
                        val tipoEventoId : Long, val fechaEvento : LocalDateTime, val capacidad: Capacidad)

data class EventoHoraDTO(val id : Long, val nombre : String, val codigo : String, val inicio : LocalDateTime, val fin : LocalDateTime)

data class EventoVerDTO(val id: Long, val nombre: String, val codigo: String, val inicio: LocalDateTime,
                   val fin: LocalDateTime, val tipoEventoNombre: String, val capacidad: Capacidad,
                   val extraOtro: Double, val descuento: Long, val listaExtra: List<ExtraDTO>,
                   val listaExtraVariable: List<EventoExtraVariableDTO>,
                   val cateringOtro: Double, val cateringOtroDescription: String,
                   val listaExtraTipoCatering: List<ExtraDTO>,
                   val listaExtraCateringVariable: List<EventoExtraVariableDTO>, val encargado: UsuarioAbmDTO,
                   val cliente: Usuario, val presupuesto: Double, val estado: Estado, val anotaciones: String)

data class EventoBuscarFechaDTO(val empresaId : Long, val desde : LocalDateTime, val hasta : LocalDateTime)

data class EventoConUsuarioDTO(
    val id: Long,
    val nombre: String,
    val codigo: String,
    val usuarioId: Long,
    val usuarioNombre: String,
    val usuarioApellido: String,
    val usuarioUsername: String
)

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