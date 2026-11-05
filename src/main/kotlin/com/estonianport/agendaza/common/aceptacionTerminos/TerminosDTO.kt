package com.estonianport.agendaza.common.aceptacionTerminos

import java.io.Serializable

data class TerminosDTO(
    val version: String,
    val texto: String,
    val hash: String
) : Serializable
