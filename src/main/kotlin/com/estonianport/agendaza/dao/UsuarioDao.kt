package com.estonianport.agendaza.dao

import com.estonianport.agendaza.model.Usuario
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UsuarioDao : CrudRepository<Usuario, Long> {
    fun getByUsername(username: String): Usuario

    fun findOneByUsername(username: String): Usuario?

}