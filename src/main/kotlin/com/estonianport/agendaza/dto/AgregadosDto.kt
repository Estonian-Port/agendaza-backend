package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.Extra

class AgregadosDto(val id: Long, val extraOtro : Long, val descuento : Long,
                   val listaExtra : MutableSet<ExtraDto>, val listaExtraVariable : MutableSet<ExtraVariableReservaDto>) {}

