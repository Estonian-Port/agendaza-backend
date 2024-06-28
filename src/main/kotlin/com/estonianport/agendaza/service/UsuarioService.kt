package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dto.EventoDto
import com.estonianport.agendaza.repository.UsuarioRepository
import com.estonianport.agendaza.dto.GenericItemDto
import com.estonianport.agendaza.dto.UsuarioAbmDto
import com.estonianport.agendaza.model.Usuario
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
class UsuarioService : GenericServiceImpl<Usuario, Long>() {

    @Autowired
    lateinit var usuarioRepository: UsuarioRepository

    override val dao: CrudRepository<Usuario, Long>
        get() = usuarioRepository

    fun getUsuarioIdByUsername(username: String): Long {
        return usuarioRepository.getByUsername(username).id
    }
    fun getAllUsuariosByEmpresaId(id : Long, pageNumber : Int): List<UsuarioAbmDto> {
        return usuarioRepository.findAll(id, PageRequest.of(pageNumber,10)).content
            .map {
                UsuarioAbmDto(it.id, it.nombre, it.apellido , it.username)
            }
    }
    fun getAllUsersByFilterName(id : Long, pageNumber : Int, buscar: String): List<UsuarioAbmDto>{
        return usuarioRepository.usuariosByNombre(id, buscar, PageRequest.of(pageNumber,10)).content
            .map {
                UsuarioAbmDto(it.id, it.nombre, it.apellido , it.username)
            }
    }
    fun contadorDeUsuarios(id : Long): Int {
        return usuarioRepository.cantidadDeUsuarios(id)
    }
    fun contadorDeUsuariosFiltrados(id : Long, buscar : String): Int {
        return usuarioRepository.cantidadDeUsuariosFiltrados(id,buscar)
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
