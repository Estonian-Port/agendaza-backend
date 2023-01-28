package com.estonianport.agendaza.common.security

import com.estonianport.agendaza.dao.UsuarioDao
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.Usuario
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class UserDetailServiceImpl : UserDetailsService {

    @Autowired
    lateinit var usuarioDao : UsuarioDao

    override fun loadUserByUsername(email: String): UserDetails{

        val usuario : Usuario = usuarioDao.findOneByMail(email)
            ?: throw NotFoundException("No se encontró el itinerario de id ")


        //TODO  Si no lo encuentra throws el usuario no existe en el DAO

        return UserDetailImpl(usuario)
    }

}