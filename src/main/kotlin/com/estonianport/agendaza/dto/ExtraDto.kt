package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.TipoExtra


class ExtraDto(val id : Long, val nombre : String, val tipoExtra : TipoExtra, val empresaId : Long, val precio : Double){

    var listaTipoEventoId: MutableSet<Long> = mutableSetOf()

}

class EventoExtraVariableDto(val id : Long, val cantidad : Int, val nombre : String, val precio : Double){}

