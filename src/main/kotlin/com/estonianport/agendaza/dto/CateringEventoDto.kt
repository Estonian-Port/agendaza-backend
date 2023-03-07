package com.estonianport.agendaza.dto

class CateringEventoDto(val id: Long, val cateringOtro : Long, val presupuesto : Long, val descripcion : String,
                        val listaExtraTipoCatering : Array<Long>, val listaExtraCateringVariable : Array<ExtraVariableReservaDto>) {}
