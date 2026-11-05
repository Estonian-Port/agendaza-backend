package com.estonianport.agendaza.common.aceptacionTerminos

import org.springframework.data.repository.CrudRepository

interface AceptacionTerminosRepository : CrudRepository<AceptacionTerminos, Long> {
    fun existsByUsuarioIdAndVersion(usuarioId: Long, version: String): Boolean
}

