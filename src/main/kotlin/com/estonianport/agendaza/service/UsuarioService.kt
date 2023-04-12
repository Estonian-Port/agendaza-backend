package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.repository.UsuarioRepository
import com.estonianport.agendaza.dto.GenericItemDto
import com.estonianport.agendaza.model.Cargo
import com.estonianport.agendaza.model.Usuario
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class UsuarioService : GenericServiceImpl<Usuario, Long>() {

    @Autowired
    lateinit var usuarioRepository: UsuarioRepository

    override val dao: CrudRepository<Usuario, Long>
        get() = usuarioRepository

    fun getUsuarioIdByUsername(username: String): Long {
        return usuarioRepository.getByUsername(username).id
    }

    fun getAllEmpresaByUsuario(usuario : Usuario) : List<GenericItemDto>{
        return usuario.listaCargo.map { GenericItemDto(it.empresa.id, it.empresa.nombre) }
    }

    fun getUsuarioByEmail(email : String) : Usuario?{
        return usuarioRepository.getUsuarioByEmail(email)
    }

    fun getUsuarioByCelular(celular : Long): Usuario?{
        return usuarioRepository.getUsuarioByCelular(celular)
    }

    fun findById(id : Long) : Usuario? {
        return usuarioRepository.findById(id).get()
    }
}
