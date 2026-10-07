package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.enums.TipoExtra
import java.time.LocalDateTime


class ExtraDTO(val id : Long, val nombre : String, val tipoExtra : TipoExtra){

    var empresaId : Long = 0

    var precio : Double = 0.0

    var listaTipoEventoId: MutableSet<Long> = mutableSetOf()

}

class ExtraPrecioDTO(val id: Long, val nombre: String, val tipoExtra: TipoExtra, val precio: Double)

class EventoExtraVariableDTO(val id : Long, val cantidad : Int, val nombre : String, val precio : Double)

fun Extra.toDTO(): ExtraDTO {
    return ExtraDTO(id, nombre, tipoExtra)
}

fun Extra.toExtraPrecioDTO(empresa: Empresa, fechaEvento: LocalDateTime): ExtraDTO {
    val extraDTO = ExtraDTO(id, nombre, tipoExtra)
    extraDTO.precio = empresa.getPrecioOfExtraByFecha(this, fechaEvento)
    return extraDTO
}