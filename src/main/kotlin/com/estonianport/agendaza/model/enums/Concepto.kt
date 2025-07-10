package com.estonianport.agendaza.model.enums

import com.estonianport.agendaza.model.Pago

enum class Concepto {
    CUOTA {
        override fun getDescripcion(pago: Pago): String {
            return if (pago.numeroCuota != null) "Cuota Nº${pago.numeroCuota}" else "Cuota"
        }
    },
    SENIA {
        override fun getDescripcion(pago: Pago): String = "Seña"
    },
    PAGO_TOTAL {
        override fun getDescripcion(pago: Pago): String = "Pago Total"
    },
    ADELANTO {
        override fun getDescripcion(pago: Pago): String = "Adelanto"
    };

    abstract fun getDescripcion(pago: Pago): String
}