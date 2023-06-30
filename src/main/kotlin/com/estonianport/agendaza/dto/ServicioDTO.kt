package com.estonianport.agendaza.dto

class ServicioDTO(val id : Long, val nombre : String, val empresaId : Long){

    var listaTipoEventoId: List<Long> = mutableListOf()

}
