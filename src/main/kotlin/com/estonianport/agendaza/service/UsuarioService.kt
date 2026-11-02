package com.estonianport.agendaza.service

import com.estonianport.agendaza.common.GenericServiceImpl
import com.estonianport.agendaza.common.aceptacionTerminos.TerminosService
import com.estonianport.agendaza.dto.*
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.repository.UsuarioRepository
import com.estonianport.agendaza.model.Usuario
import com.estonianport.agendaza.model.Cargo
import com.estonianport.agendaza.common.codeGeneratorUtil.CodeGeneratorUtil
import com.estonianport.agendaza.common.emailService.EmailService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class UsuarioService (
    val usuarioRepository: UsuarioRepository,
    val empresaService: EmpresaService? = null,
    val cargoService: CargoService? = null,
    val emailService: EmailService? = null,
    val passwordEncoder: BCryptPasswordEncoder? = null,
    val terminosService: TerminosService? = null
): GenericServiceImpl<Usuario, Long>() {

    override val dao: CrudRepository<Usuario, Long>
        get() = usuarioRepository

    // ==================== BÚSQUEDAS BÁSICAS ====================

    @Transactional(readOnly = true)
    fun getByEmail(email: String): Usuario? {
        return usuarioRepository.findAllByEmail(email).firstOrNull()
    }

    @Transactional(readOnly = true)
    fun getByCelular(celular: Long): Usuario? {
        return usuarioRepository.findAllByCelular(celular).firstOrNull()
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): Usuario {
        return usuarioRepository.findById(id).orElseThrow {
            NotFoundException("Usuario no encontrado")
        }
    }

    // ==================== DTOs ====================

    @Transactional(readOnly = true)
    @Cacheable(value = ["usuarioDtoByEmail"], key = "#email")
    fun getUsuarioDtoByEmail(email: String): UsuarioResponseDto {
        return usuarioRepository.getUsuarioDtoByEmail(email)
            ?: throw NotFoundException("No se encontró un usuario con el email proporcionado")
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["usuarioDtoByCelular"], key = "#celular")
    fun getUsuarioDtoByCelular(celular: Long): UsuarioResponseDto {
        return usuarioRepository.getUsuarioDtoByCelular(celular)
            ?: throw NotFoundException("No se encontró un usuario con el celular proporcionado")
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["usuarioDtoByUsername"], key = "#username")
    fun getUsuarioDtoByUsername(username: String): UsuarioResponseDto {
        return usuarioRepository.getUsuarioDtoByUsername(username)
            ?: throw NotFoundException("No se encontró un usuario con el username proporcionado")
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["usuarioPerfil"], key = "#usuarioId")
    fun getUsuarioPerfil(usuarioId: Long): UsuarioPerfilDTO? {
        return usuarioRepository.getUsuarioPerfil(usuarioId)
            ?: throw NotFoundException("Usuario no encontrado")
    }

    /**
     * Obtiene la información de un usuario en el contexto de una empresa
     * Principalmente para obtener su cargo en esa empresa
     */
    @Transactional(readOnly = true)
    fun getUsuarioOfEmpresa(usuarioId: Long, empresaId: Long): UsuarioEditCargoDTO? {
        return usuarioRepository.getUsuarioOfEmpresa(usuarioId, empresaId)
            ?: throw NotFoundException("Usuario no encontrado")

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

    @Transactional
    fun alta(dto: AltaUsuarioDTO): Usuario {
        val empresa = empresaService!!.findById(dto.empresaId)
        var usuario = getByEmail(dto.email)
        var temporaryPassword: String? = null

        val usuarioConCredenciales = usuario?.username?.isNotBlank() == true &&
                usuario.password?.isNotBlank() == true
        if (usuario == null || !usuarioConCredenciales) {
            temporaryPassword = CodeGeneratorUtil.generarCodigoBase26(8)
            usuario = usuario ?: Usuario(
                0,
                "",
                "",
                CodeGeneratorUtil.generarCelularUnico(),
                dto.email)

            usuario.username = dto.email
            usuario.password = passwordEncoder!!.encode(temporaryPassword)
            usuario.debeActualizarDatos = true
            usuario = save(usuario)
        }

        cargoService!!.save(Cargo(0, usuario, empresa, dto.rol))

        if (temporaryPassword != null) {
            emailService!!.enviarInvitacion(dto.email, dto.email, temporaryPassword!!, empresa, dto.rol)
        } else {
            emailService!!.enviarNotificacionNuevaAgenda(dto.email, empresa, dto.rol)
        }
        return usuario
    }

    @Transactional
    @CacheEvict(value = ["onboardingPendiente", "usuarioDtoByUsername", "usuarioPerfil"], allEntries = true)
    fun completarDatos(username: String, dto: CompletarDatosUsuarioDTO, ip: String?, userAgent: String?): Usuario {
        val usuario = usuarioRepository.getByUsername(username)
            ?: throw NotFoundException("Usuario no encontrado")

        require(dto.aceptaTerminos && dto.versionTerminos == TerminosService.VERSION_VIGENTE) {
            "Debés aceptar los términos y condiciones vigentes"
        }
        require(dto.nombre.isNotBlank() && dto.apellido.isNotBlank()) { "Nombre y apellido son obligatorios" }
        require(dto.fechaNacimiento.isBefore(LocalDate.now().minusYears(18))) { "Fecha de nacimiento inválida" }
        require(dto.password.length >= 8) { "La contraseña debe tener al menos 8 caracteres" }
        require(!passwordEncoder!!.matches(dto.password, usuario.password)) {
            "La nueva contraseña debe ser distinta de la temporal"
        }
        if (usuarioRepository.findAllByCelular(dto.celular).any { it.id != usuario.id })
            throw IllegalArgumentException("Celular ya registrado")
        usuarioRepository.getByUsername(dto.username)?.takeIf { it.id != usuario.id }
            ?.let { throw IllegalArgumentException("Username no disponible") }

        val guardado = save(dto.updateUsuario(usuario, passwordEncoder!!.encode(dto.password)))
        terminosService?.registrarAceptacion(guardado.id, ip, userAgent)
        return guardado
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
    @CacheEvict(value = [
        "usuarioDtoByEmail",
        "usuarioDtoByCelular",
        "usuarioDtoByUsername",
        "usuarioPerfil",
        "eventoVer"
    ], key = "#entity.username", allEntries = true)
    override fun save(entity: Usuario): Usuario {
        return usuarioRepository.save(entity)
    }

    // ==================== DELETE ====================

    @Transactional
    @CacheEvict(value = ["usuarioPerfil"], key = "#id")
    override fun delete(id: Long) {
        super.delete(id)
    }

    // ==================== VALIDACIONES ====================

    @Transactional(readOnly = true)
    fun existsByEmail(email: String): Boolean {
        return usuarioRepository.existsByEmail(email)
    }

    @Transactional(readOnly = true)
    fun existsByUsername(username: String): Boolean = usuarioRepository.existsByUsername(username)


    @Transactional(readOnly = true)
    fun existsByCelular(celular: Long): Boolean {
        return usuarioRepository.existsByCelular(celular)
    }
}
