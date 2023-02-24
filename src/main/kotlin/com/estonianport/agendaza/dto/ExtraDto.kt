package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.TipoExtra


class ExtraDto(val id : Long, val nombre : String, val tipoExtra : TipoExtra, val empresaId : Long){

    var listaTipoEventoId: MutableSet<Long> = mutableSetOf()

}