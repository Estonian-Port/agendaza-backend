package com.estonianport.agendaza.common.security

import com.estonianport.agendaza.repository.UsuarioRepository
import com.estonianport.agendaza.model.Usuario
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailServiceImpl : UserDetailsService {

    @Autowired
    lateinit var usuarioRepository : UsuarioRepository

    override fun loadUserByUsername(username: String): UserDetails{

        val usuario : Usuario = usuarioRepository.getByUsername(username)
            ?: throw UsernameNotFoundException("No se encontró el usuario")

        return UserDetailImpl(usuario)
    }

}