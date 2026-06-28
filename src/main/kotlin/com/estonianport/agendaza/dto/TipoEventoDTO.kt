package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.TipoEvento
import com.estonianport.agendaza.model.enums.Duracion
import java.time.LocalTime

class TipoEventoDTO(val id : Long, val nombre : String, val cantidadDuracion: LocalTime, val duracion: Duracion,
                    val capacidadAdultos : Int, val capacidadNinos : Int, val empresaId: Long)

class TipoEventoPrecioDTO(val id : Long, val nombre : String, val precio : Double)

class TimeDTO(val hour: Int, val minute: Int)

fun TipoEvento.toDTO() : TipoEventoDTO {
    return TipoEventoDTO(id, nombre, LocalTime.of(cantidadDuracion.hour, cantidadDuracion.minute),
        duracion, capacidadAdultos, capacidadNinos, empresa.id)
}

fun TipoEvento.toEventoCapacidadDTO() : EventoCapacidadDTO {
    return EventoCapacidadDTO(capacidadAdultos, capacidadNinos)
}