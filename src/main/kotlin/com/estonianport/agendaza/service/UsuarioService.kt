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
import com.estonianport.agendaza.errors.BadRequestException
import com.estonianport.agendaza.errors.ConflictException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.security.access.AccessDeniedException
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class UsuarioService (
    val usuarioRepository: UsuarioRepository,
    val empresaService: EmpresaService? = null,
    val cargoService: CargoService? = null,
    val emailService: EmailService? = null,
    val passwordEncoder: PasswordEncoder? = null,
    val terminosService: TerminosService? = null,
    val refreshTokenService: com.estonianport.agendaza.common.security.RefreshTokenService? = null
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
    fun debeCambiarPassword(username: String): Boolean =
        usuarioRepository.getByUsername(username)?.debeCambiarPassword ?: false

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
            usuario.debeCambiarPassword = true
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

        if (!dto.aceptoTerminos || dto.versionTerminos != TerminosService.VERSION_VIGENTE) {
            throw BadRequestException("Debés aceptar los términos y condiciones vigentes")
        }
        if (dto.nombre.isBlank() || dto.apellido.isBlank()) {
            throw BadRequestException("Nombre y apellido son obligatorios")
        }
        val emailRegex = Regex("^[A-Za-z0-9._%+-]{3,}@[A-Za-z0-9.-]+$")
        if (!emailRegex.matches(dto.email.trim())) {
            throw BadRequestException("El email debe tener al menos 3 caracteres antes de @ y un dominio válido")
        }
        if (!dto.fechaNacimiento.isBefore(LocalDate.now().minusYears(18))) {
            throw BadRequestException("Fecha de nacimiento inválida")
        }
        val passwordRegex = Regex("^(?=.*[A-Z])(?=.*\\d).+$")

        if (dto.password.length < 8) {
            throw BadRequestException("La contraseña debe tener al menos 8 caracteres")
        }
        if (!passwordRegex.matches(dto.password)) {
            throw BadRequestException("La contraseña debe contener al menos una letra mayúscula y un número")
        }
        val esFormatoTemporal = Regex("^[A-Z]{8}$").matches(dto.password)
        if (esFormatoTemporal && passwordEncoder!!.matches(dto.password, usuario.password)) {
            throw BadRequestException("La nueva contraseña debe ser distinta de la temporal")
        }
        if (usuarioRepository.findAllByCelular(dto.celular).any { it.id != usuario.id }) {
            throw ConflictException("El número de celular ya se encuentra registrado")
        }
        if (usuarioRepository.findAllByEmail(dto.email).any { it.id != usuario.id }) {
            throw ConflictException("El email ya se encuentra registrado")
        }
        usuarioRepository.getByUsername(dto.username)?.takeIf { it.id != usuario.id }?.let {
            throw ConflictException("El nombre de usuario no está disponible")
        }

        val guardado = save(dto.updateUsuario(usuario, passwordEncoder!!.encode(dto.password)))
        guardado.debeCambiarPassword = false
        save(guardado)
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
    fun saveUsuario(dto: UsuarioDTORequest): Usuario {
        val usuario = dto.usuario

        if (usuario.id == 0L) {
            usuario.password = requireNotNull(passwordEncoder) {
                "PasswordEncoder no configurado"
            }.encode(usuario.password)
        } else {
            usuario.password = findById(usuario.id).password.orEmpty()
        }

        val usuarioGuardado = save(usuario)
        actualizarCargo(usuarioGuardado, dto.empresaId, dto.cargo)
        return usuarioGuardado
    }

    @Transactional
    fun updatePassword(adminUsername: String, usuarioId: Long, password: String) {
        val cargoService = cargoService ?: throw AccessDeniedException("Servicio de cargos no configurado")
        if (!cargoService.esAdminDeUsuario(adminUsername, usuarioId)) {
            throw AccessDeniedException("Solo un administrador de la empresa puede actualizar contraseñas")
        }
        val usuario = findById(usuarioId)
        validarPassword(password)
        usuario.password = requireNotNull(passwordEncoder) {
            "PasswordEncoder no configurado"
        }.encode(password)
        usuario.debeCambiarPassword = false
        usuario.fechaCambioPassword = LocalDateTime.now()
        save(usuario)
        refreshTokenService?.revocarTodos(requireNotNull(usuario.username))
    }

    @Transactional
    fun restablecerPassword(adminUsername: String, usuarioId: Long) {
        val cargoService = cargoService ?: throw AccessDeniedException("Servicio de cargos no configurado")
        if (!cargoService.esAdminDeUsuario(adminUsername, usuarioId)) {
            throw AccessDeniedException("Solo un administrador de la empresa puede restablecer contraseñas")
        }

        val usuario = findById(usuarioId)
        val cargo = cargoService.getCargoDeUsuarioParaAdmin(adminUsername, usuarioId)
            ?: throw NotFoundException("El usuario no tiene una empresa activa")
        val temporaryPassword = CodeGeneratorUtil.generarCodigoBase26(8)

        usuario.password = requireNotNull(passwordEncoder) { "PasswordEncoder no configurado" }
            .encode(temporaryPassword)
        usuario.debeCambiarPassword = true
        usuario.fechaCambioPassword = LocalDateTime.now()
        save(usuario)
        refreshTokenService?.revocarTodos(requireNotNull(usuario.username))
        emailService?.enviarRestablecimientoPassword(usuario.email, temporaryPassword, cargo.empresa)
    }

    @Transactional
    @CacheEvict(value = ["onboardingPendiente", "usuarioDtoByUsername", "usuarioPerfil"], allEntries = true)
    fun cambiarPassword(username: String, password: String): Usuario {
        val usuario = usuarioRepository.getByUsername(username)
            ?: throw NotFoundException("Usuario no encontrado")
        validarPassword(password)
        usuario.password = requireNotNull(passwordEncoder) { "PasswordEncoder no configurado" }.encode(password)
        usuario.debeCambiarPassword = false
        usuario.fechaCambioPassword = LocalDateTime.now()
        return save(usuario)
    }

    fun validarPassword(password: String) {
        if (password.length < 8) throw BadRequestException("La contraseña debe tener al menos 8 caracteres")
        if (!Regex("^(?=.*[A-Z])(?=.*\\d).+$").matches(password)) {
            throw BadRequestException("La contraseña debe contener al menos una letra mayúscula y un número")
        }
    }

    private fun actualizarCargo(
        usuario: Usuario,
        empresaId: Long,
        tipoCargo: com.estonianport.agendaza.model.enums.TipoCargo?
    ) {
        if (empresaId == 0L || tipoCargo == null) return

        val empresa = empresaService?.get(empresaId) ?: return
        val cargoService = requireNotNull(cargoService) {
            "CargoService no configurado"
        }
        val cargoExistente = empresa.listaEmpleados.find { it.usuario.id == usuario.id }

        if (cargoExistente != null) {
            cargoExistente.tipoCargo = tipoCargo
            cargoService.save(cargoExistente)
        } else {
            cargoService.save(Cargo(0, usuario, empresa, tipoCargo))
        }
    }

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
    override fun delete(id: Long) {
        val usuario = findById(id)
        usuario.fechaBaja = LocalDate.now()
        save(usuario)
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

    fun getByUsername(username: String) : Usuario ?{
        return usuarioRepository.getByUsername(username)
    }
}
