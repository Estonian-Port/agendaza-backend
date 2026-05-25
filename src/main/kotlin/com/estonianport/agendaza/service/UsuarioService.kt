package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dto.*
import com.estonianport.agendaza.repository.UsuarioRepository
import com.estonianport.agendaza.model.Usuario
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UsuarioService : GenericServiceImpl<Usuario, Long>() {

    @Autowired
    lateinit var usuarioRepository: UsuarioRepository

    override val dao: CrudRepository<Usuario, Long>
        get() = usuarioRepository

    // ==================== BÚSQUEDAS BÁSICAS ====================

    @Transactional(readOnly = true)
    fun getByEmail(email: String): Usuario? {
        return usuarioRepository.getByEmail(email)
    }

    @Transactional(readOnly = true)
    fun getByCelular(celular: Long): Usuario? {
        return usuarioRepository.getByCelular(celular)
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): Usuario? {
        return usuarioRepository.findById(id).orElse(null)
    }

    // ==================== DTOs ====================

    @Transactional(readOnly = true)
    fun getUsuarioDtoByEmail(email: String): UsuarioResponseDto {
        return usuarioRepository.getUsuarioDtoByEmail(email)
            ?: throw NoSuchElementException("No se encontró un usuario con el email proporcionado")
    }

    @Transactional(readOnly = true)
    fun getUsuarioPerfil(usuarioId: Long): UsuarioPerfilDTO {
        return usuarioRepository.getUsuarioPerfil(usuarioId)
            ?: throw NoSuchElementException("Usuario no encontrado")
    }

    // ==================== EMPLEADOS ====================

    @Transactional(readOnly = true)
    fun getAllUsuario(empresaId: Long, pageNumber: Int): List<UsuarioAbmDTO> {
        return usuarioRepository.getAllUsuario(empresaId, PageRequest.of(pageNumber, 10)).content
    }

    @Transactional(readOnly = true)
    fun getAllUsuarioFiltrados(empresaId: Long, pageNumber: Int, buscar: String): List<UsuarioAbmDTO> {
        return usuarioRepository.getAllUsuarioFiltrados(empresaId, buscar, PageRequest.of(pageNumber, 10)).content
    }

    @Transactional(readOnly = true)
    fun getCantidadUsuario(empresaId: Long): Int {
        return usuarioRepository.getCantidadUsuario(empresaId)
    }

    @Transactional(readOnly = true)
    fun getCantidadFiltrados(empresaId: Long, buscar: String): Int {
        return usuarioRepository.getCantidadFiltrados(empresaId, buscar)
    }

    // ==================== CLIENTES ====================

    @Transactional(readOnly = true)
    fun getAllCliente(empresaId: Long, pageNumber: Int): List<UsuarioAbmDTO> {
        return usuarioRepository.getAllCliente(empresaId, PageRequest.of(pageNumber, 10)).content
    }

    @Transactional(readOnly = true)
    fun getAllClienteFiltrados(empresaId: Long, pageNumber: Int, buscar: String): List<UsuarioAbmDTO> {
        return usuarioRepository.getAllClienteFiltrados(empresaId, buscar, PageRequest.of(pageNumber, 10)).content
    }

    @Transactional(readOnly = true)
    fun getCantidadCliente(empresaId: Long): Int {
        return usuarioRepository.getCantidadCliente(empresaId)
    }

    @Transactional(readOnly = true)
    fun getCantidadClienteFiltrados(empresaId: Long, buscar: String): Int {
        return usuarioRepository.getCantidadClienteFiltrados(empresaId, buscar)
    }

    // ==================== EMPRESAS ====================

    @Transactional(readOnly = true)
    fun getAllEmpresaByUsuarioId(usuarioId: Long): List<EmpresaAbmDTO> {
        return usuarioRepository.getAllEmpresaByUsuarioId(usuarioId)
    }

    // ==================== SAVE/UPDATE ====================

    @Transactional
    @CacheEvict(value = ["usuarioByUsername"], key = "#entity.username")
    override fun save(entity: Usuario): Usuario {
        return usuarioRepository.save(entity)
    }

    // ==================== VALIDACIONES ====================

    @Transactional(readOnly = true)
    fun existsByEmail(email: String): Boolean {
        return usuarioRepository.existsByEmail(email)
    }

    @Transactional(readOnly = true)
    fun existsByCelular(celular: Long): Boolean {
        return usuarioRepository.existsByCelular(celular)
    }

}
