package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.Extra

class AgregadosDto(val id: Long, val extraOtro : Long, val descuento : Long,
                   val listaExtra : Array<Long>, val listaExtraVariable : Array<ExtraVariableReservaDto>) {}

class AgregadosEditDto(val id: Long, val extraOtro : Long, val descuento : Long,
                       val listaExtra : MutableSet<ExtraDto>, var listaExtraVariable : MutableSet<ExtraVariableDto>) {}
