package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.TipoExtra


class ExtraDTO(val id : Long, val nombre : String, val tipoExtra : TipoExtra, val empresaId : Long){

    var precio : Double = 0.0

    var listaTipoEventoId: List<Long> = mutableListOf()

}

class EventoExtraVariableDTO(val id : Long, val cantidad : Int, val nombre : String, val precio : Double){}

