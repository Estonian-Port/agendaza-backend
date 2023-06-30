package com.estonianport.agendaza.dto

class GenericItemDto(var id: Long, var nombre: String) {

    var empresaId : Long = 0

    var listaTipoEventoId: MutableSet<Long> = mutableSetOf()
}