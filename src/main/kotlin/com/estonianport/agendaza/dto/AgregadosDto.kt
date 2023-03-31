package com.estonianport.agendaza.dto

class AgregadosDto(val id: Long, val extraOtro : Long, val descuento : Long,
                   val listaExtra : List<ExtraDto>, val listaExtraVariable : List<EventoExtraVariableDto>) {}

