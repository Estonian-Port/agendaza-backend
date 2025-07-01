package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.enums.TipoExtra


class ExtraDTO(val id : Long, val nombre : String, val tipoExtra : TipoExtra){

    var empresaId : Long = 0

    var precio : Double = 0.0

    var listaTipoEventoId: List<Long> = mutableListOf()

}

class EventoExtraVariableDTO(val id : Long, val cantidad : Int, val nombre : String, val precio : Double){}

