package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.TipoEvento

class GenericItemDto(var id: Long, var nombre: String) {

    var empresaId : Long = 0

    var listaTipoEventoId: MutableSet<Long> = mutableSetOf()
}