package com.estonianport.agendaza.model.enums

enum class TipoGasto {
    // Gastos vinculados a un evento (subcategorías)
    EVENTO,
    MERCADERIA,
    EMPLEADOS,
    LIBRERIA,

    // Reinversión
    MANTENIMIENTO,
    DECORACION,
    EQUIPAMIENTO,

    // Gastos fijos
    SERVICIOS,
    IMPUESTOS,
    SUELDOS,
    TARJETA_CREDITO,

    OTROS
}