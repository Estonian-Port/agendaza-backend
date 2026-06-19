package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.enums.TipoCargo
import java.io.Serializable

data class AgendaDTO(
    var id: Long,
    var nombre: String,
    var rol : TipoCargo
): Serializable