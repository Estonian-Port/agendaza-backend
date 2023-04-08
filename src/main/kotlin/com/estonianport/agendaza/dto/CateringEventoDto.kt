package com.estonianport.agendaza.dto

class CateringEventoDto(val id: Long, val cateringOtro : Long, val presupuesto : Double, val descripcion : String,
                        val listaExtraTipoCatering : List<ExtraDto>, val listaExtraCateringVariable : List<EventoExtraVariableDto>) {}
