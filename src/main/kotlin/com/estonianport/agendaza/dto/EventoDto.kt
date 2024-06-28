package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.Capacidad
import com.estonianport.agendaza.model.Estado
import com.estonianport.agendaza.model.Usuario
import java.time.LocalDateTime

class EventoDto(var id: Long, var nombre: String, var codigo : String,
                var inicio : LocalDateTime, var fin : LocalDateTime, var tipoEvento : String) {}

class EventoUsuarioDto(var id: Long, var nombre: String, var codigo : String, usuario: UsuarioAbmDto)

class EventoAgendaDto(var id: Long, var title: String, var start : LocalDateTime, var end : LocalDateTime) {}

class EventoReservaDto(val id: Long, val nombre: String, var capacidad : Capacidad, var codigo : String,
                       val inicio : LocalDateTime, var fin : LocalDateTime, val tipoEventoId : Long,
                       val empresaId : Long, val extraOtro : Long, val descuento : Long,
                       val listaExtra : List<ExtraDTO>, val listaExtraVariable : List<EventoExtraVariableDTO>,
                       val cateringOtro : Double, val cateringOtroDescripcion : String,
                       val listaExtraTipoCatering : List<ExtraDTO>,
                       val listaExtraCateringVariable : List<EventoExtraVariableDTO>,
                       val cliente : Usuario, val encargadoId : Long, val estado : Estado, val anotaciones : String) {}

class EventoPagoDto(val id : Long, val nombre : String, val codigo : String,
                      val precioTotal : Double, val listaPagos : List<PagoDto>) {}

class EventoExtraDto(val id : Long, val nombre : String, val codigo : String,
                     val extraOtro : Long, val descuento : Long,
                     val listaExtra : List<ExtraDTO>, val listaExtraVariable : List<EventoExtraVariableDTO>,
                     val tipoEventoExtra : TipoEventoPrecioDTO, val fechaEvento : LocalDateTime) {}

class EventoCateringDto(val id : Long, val nombre : String, val codigo : String, val cateringOtro : Double,
                        val cateringOtroDescripcion : String, val listaExtraTipoCatering : List<ExtraDTO>,
                        val listaExtraCateringVariable : List<EventoExtraVariableDTO>,
                        val tipoEventoId : Long, val fechaEvento : LocalDateTime, val capacidad: Capacidad) {}

class EventoHoraDto(val id : Long, val nombre : String, val codigo : String, val inicio : LocalDateTime, val fin : LocalDateTime) {}

class EventoVerDto(val id : Long, val nombre : String, val codigo : String, val inicio : LocalDateTime,
                   val fin : LocalDateTime, val tipoEventoNombre : String, val capacidad : Capacidad,
                   val extraOtro : Long, val descuento : Long, val listaExtra : List<ExtraDTO>,
                   val listaExtraVariable : List<EventoExtraVariableDTO>,
                   val cateringOtro : Double, val cateringOtroDescription : String,
                   val listaExtraTipoCatering : List<ExtraDTO>,
                   val listaExtraCateringVariable : List<EventoExtraVariableDTO>, val encargado: UsuarioAbmDto,
                   val cliente : Usuario, val presupuesto : Double, val estado : Estado, val anotaciones: String) {}

class EventoBuscarFechaDto(val empresaId : Long, val desde : LocalDateTime, val hasta : LocalDateTime) {
}