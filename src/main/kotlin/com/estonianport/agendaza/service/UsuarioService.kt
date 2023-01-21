package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dao.UsuarioDao
import com.estonianport.agendaza.dto.UsuarioDto
import com.estonianport.agendaza.errors.NotFoundException
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

    fun getUsuarioLogIn(usuarioDto: UsuarioDto): Long {
        // Si el usuarioServices.getByUsername no encuentra el usuario
        // devuelve un 'HttpErrorResponse 500' con 'El usuario no es correcto' en error.message
        val usuarioBack = getByUsername(usuarioDto)
        if (usuarioDto.password == usuarioBack.password) {
            return usuarioBack.id
        }
        throw NotFoundException("Contraseña erronea")
    }

    fun getByUsername(usuarioDto: UsuarioDto): Usuario = usuarioDao.getByUsername(usuarioDto.username)

}
