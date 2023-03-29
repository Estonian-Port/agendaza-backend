package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.TipoExtra


class ExtraDto(val id : Long, val nombre : String, val tipoExtra : TipoExtra, val empresaId : Long){

    var listaTipoEventoId: MutableSet<Long> = mutableSetOf()

    var precio : Double = 0.0

}

class ExtraVariableDto(val id : Long, val nombre : String, val tipoExtra : TipoExtra, val cantidad : Int){

    var precio : Double = 0.0
}

class ExtraVariableReservaDto(val id : Long, val cantidad : Int){}

