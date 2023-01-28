package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.common.security.AuthCredentials
import com.estonianport.agendaza.dao.UsuarioDao
import com.estonianport.agendaza.model.Usuario
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
class UsuarioService : GenericServiceImpl<Usuario, Long>() {

    @Autowired
    lateinit var usuarioDao: UsuarioDao

    override val dao: CrudRepository<Usuario, Long>
        get() = usuarioDao


    fun getUsuarioIdByUsername(authCredentials: AuthCredentials): Long {
        return usuarioDao.getByUsername(authCredentials.username).id
    }

}
