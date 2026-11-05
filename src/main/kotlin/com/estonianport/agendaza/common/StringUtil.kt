package com.estonianport.agendaza.common

fun String.limpiarNombre() = trim().replace(Regex("\\s+"), " ").lowercase()