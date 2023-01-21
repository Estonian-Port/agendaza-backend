package com.estonianport.agendaza.dao

import com.estonianport.agendaza.model.Usuario
import org.springframework.data.repository.CrudRepository

interface UsuarioDao : CrudRepository<Usuario, Long> {
    fun getByUsername(username: String): Usuario
}