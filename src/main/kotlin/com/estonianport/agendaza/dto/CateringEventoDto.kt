package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.Extra

class CateringEventoDto(val id: Long, val cateringOtro : Long, val presupuesto : Double, val descripcion : String,
                        val listaExtraTipoCatering : MutableSet<ExtraDto>, val listaExtraCateringVariable : MutableSet<ExtraVariableReservaDto>) {}
