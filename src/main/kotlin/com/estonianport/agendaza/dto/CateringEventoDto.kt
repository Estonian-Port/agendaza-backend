package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.Extra

class CateringEventoDto(val id: Long, val cateringOtro : Long, val presupuesto : Long, val descripcion : String,
                        val listaExtraTipoCatering : Array<Long>, val listaExtraCateringVariable : Array<ExtraVariableReservaDto>) {}

class CateringEventoEditDto(val id: Long, val cateringOtro : Long, val presupuesto : Long, val descripcion : String,
                        val listaExtraTipoCatering : MutableSet<ExtraDto>, val listaExtraCateringVariable : MutableSet<ExtraVariableDto>) {}