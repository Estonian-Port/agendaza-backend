package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.Capacidad
import com.estonianport.agendaza.model.Estado
import com.estonianport.agendaza.model.Usuario
import com.fasterxml.jackson.databind.BeanDescription
import java.time.LocalDateTime

class EventoDto(var id: Long, var nombre: String, var codigo : String,
                var inicio : LocalDateTime, var fin : LocalDateTime, var tipoEvento : String) {}

class EventoAgendaDto(var id: Long, var title: String, var start : LocalDateTime, var end : LocalDateTime) {}

class EventoReservaDto(val id: Long, val nombre: String, var capacidad : Capacidad, var codigo : String,
                       val inicio : LocalDateTime, var fin : LocalDateTime, val tipoEventoId : Long,
                       val empresaId : Long, val extraOtro : Long, val descuento : Long,
                       val listaExtra : List<ExtraDto>, val listaExtraVariable : List<EventoExtraVariableDto>,
                       val cateringOtro : Double, val cateringOtroDescripcion : String,
                       val listaExtraTipoCatering : List<ExtraDto>,
                       val listaExtraCateringVariable : List<EventoExtraVariableDto>,
                       val cliente : Usuario, val encargadoId : Long, val estado : Estado) {}

class EventoPagoDto(val id : Long, val nombre : String, val codigo : String,
                      val precioTotal : Double, val listaPagos : List<PagoDto>) {}

class EventoExtraDto(val id : Long, val nombre : String, val codigo : String,
                     val extraOtro : Long, val descuento : Long,
                     val listaExtra : List<ExtraDto>, val listaExtraVariable : List<EventoExtraVariableDto>,
                     val tipoEventoExtra : TipoEventoExtraDto, val fechaEvento : LocalDateTime) {}

class EventoCateringDto(val id : Long, val nombre : String, val codigo : String, val cateringOtro : Double,
                        val cateringOtroDescripcion : String, val listaExtraTipoCatering : List<ExtraDto>,
                        val listaExtraCateringVariable : List<EventoExtraVariableDto>,
                        val tipoEventoId : Long, val fechaEvento : LocalDateTime, val capacidad: Capacidad) {}

class EventoHoraDto(val id : Long, val nombre : String, val codigo : String, val inicio : LocalDateTime, val fin : LocalDateTime) {}

class EventoVerDto(val id : Long, val nombre : String, val codigo : String, val inicio : LocalDateTime,
                   val fin : LocalDateTime, val tipoEventoNombre : String, val capacidad : Capacidad,
                   val extraOtro : Long, val descuento : Long, val listaExtra : List<ExtraDto>,
                   val listaExtraVariable : List<EventoExtraVariableDto>,
                   val cateringOtro : Double, val cateringOtroDescription : String,
                   val listaExtraTipoCatering : List<ExtraDto>,
                   val listaExtraCateringVariable : List<EventoExtraVariableDto>,
                   val cliente : Usuario, val presupuesto : Double, val estado : Estado) {}

class EventoBuscarFechaDto(val empresaId : Long, val desde : LocalDateTime, val hasta : LocalDateTime) {
}