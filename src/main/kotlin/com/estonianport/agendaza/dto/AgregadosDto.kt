package com.estonianport.agendaza.dto

import java.time.LocalDateTime

class AgregadosDto(val id: Long, val extraOtro : Long, val descuento : Long,
                   val listaExtra : Array<Long>, val listaExtraVariable : Array<ExtraVariableReservaDto>) {}
