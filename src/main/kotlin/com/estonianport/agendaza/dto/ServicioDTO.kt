package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.Servicio

class ServicioDTO(val id: Long, val nombre: String){

    var listaTipoEventoId: List<Long> = mutableListOf()
}

fun Servicio.toDTO(): ServicioDTO {
    return ServicioDTO(id, nombre).apply {
        listaTipoEventoId = listaTipoEvento.map { it.id }
    }
}
