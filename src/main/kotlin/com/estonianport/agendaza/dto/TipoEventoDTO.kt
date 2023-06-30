package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.Capacidad
import com.estonianport.agendaza.model.Duracion
import java.time.LocalTime

class TipoEventoDTO(val id : Long, val nombre : String, val cantidadDuracion: LocalTime, val duracion: Duracion,
                    var capacidad : Capacidad, val empresaId : Long) {}

class TipoEventoPrecioDTO(val id : Long, val nombre : String, val precio : Double)

class TimeDTO(val hour: Int, val minute: Int)
