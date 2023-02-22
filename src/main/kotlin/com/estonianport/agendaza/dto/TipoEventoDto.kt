package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.Capacidad
import com.estonianport.agendaza.model.Duracion
import java.time.LocalTime

class TipoEventoDto(val id : Long, val nombre : String, val cantidadDuracion: TimeDto, val duracion: Duracion,
                    var capacidad : Capacidad, val empresaId : Long) {}

class TimeDto(val hour: Int, val minute: Int)