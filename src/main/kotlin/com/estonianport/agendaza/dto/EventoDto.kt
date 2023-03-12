package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.Capacidad
import com.estonianport.agendaza.model.Estado
import com.estonianport.agendaza.model.Pago
import com.estonianport.agendaza.model.Usuario
import java.time.LocalDateTime

class EventoDto(var id: Long, var nombre: String, var codigo : String,
                var inicio : LocalDateTime, var fin : LocalDateTime, var tipoEvento : String) {}

class EventoAgendaDto(var id: Long, var title: String, var start : LocalDateTime, var end : LocalDateTime) {}
class EventoReservaDto(val id: Long, val nombre: String, var capacidad : Capacidad, var codigo : String,
                       val inicio : LocalDateTime, var fin : LocalDateTime, val tipoEventoId : Long,
                       val empresaId : Long, val agregados: AgregadosDto, val catering : CateringEventoDto,
                       val cliente : Usuario, val presupuesto : Long, val encargadoId : Long, val estado : Estado) {}

class EventoPagoDto(val id : Long, val nombre : String, val codigo : String,
                      val precioTotal : Long, val listaPagos : MutableSet<PagoDto>) {}

class EventoExtraDto(val id : Long, val nombre : String, val codigo : String,
                    val presupuesto : Long, val agregados : AgregadosEditDto) {}

class EventoCateringDto(val id : Long, val nombre : String, val codigo : String, val catering : CateringEventoEditDto) {}
