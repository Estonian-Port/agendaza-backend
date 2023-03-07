package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.Agregados
import com.estonianport.agendaza.model.Capacidad
import com.estonianport.agendaza.model.CateringEvento
import com.estonianport.agendaza.model.Estado
import com.estonianport.agendaza.model.Usuario
import java.time.LocalDateTime

class EventoDto(var id: Long, var nombre: String, var codigo : String,
                var inicio : LocalDateTime, var fin : LocalDateTime, var tipoEvento : String) {}

class EventoReservaDto(val id: Long, val nombre: String, var capacidad : Capacidad, var codigo : String,
                       val inicio : LocalDateTime, var fin : LocalDateTime, val tipoEventoId : Long,
                       val empresaId : Long, val agregados: AgregadosDto, val catering : CateringEventoDto,
                       val cliente : Usuario, val presupuesto : Long, val encargadoId : Long, val estado : Estado) {}