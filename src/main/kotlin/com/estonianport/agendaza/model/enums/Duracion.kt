package com.estonianport.agendaza.model.enums

import com.estonianport.agendaza.errors.BusinessException

enum class Duracion {
    CORTO, MEDIO, LARGO;

    companion object {
        fun fromString(value: String): Duracion =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: throw BusinessException("La duración '$value' no es válida")
    }
}
