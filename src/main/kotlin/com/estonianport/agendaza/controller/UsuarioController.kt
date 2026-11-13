package com.estonianport.agendaza.controller

import com.estonianport.agendaza.common.aceptacionTerminos.OnboardingService
import com.estonianport.agendaza.dto.*
import com.estonianport.agendaza.dto.response.CustomResponse
import com.estonianport.agendaza.service.UsuarioService
import com.estonianport.agendaza.common.security.UsernameNormalizer
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/v1/usuarios")
@CrossOrigin("*")
class UsuarioController(
    private val usuarioService: UsuarioService,
    private val onboardingService: OnboardingService
){

    // ==================== AUTENTICACIÓN ====================

    /**
     * Obtiene el usuario logueado desde el token JWT (principal de Spring Security)
     */
    @GetMapping("/me")
    fun getUsuarioLogueado(principal: Principal): ResponseEntity<CustomResponse<UsuarioResponseDto>> {
        val usuarioDto = usuarioService.getUsuarioDtoByUsername(principal.name)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Usuario obtenido correctamente",
                data = usuarioDto
            )
        )
    }

    @GetMapping("/me/estado")
    fun estado(principal: Principal) =
        ResponseEntity.ok(
        CustomResponse(
            message = "Estado",
            data = EstadoAccesoDTO(
                onboardingRequired = onboardingService.pendiente(principal.name),
                passwordChangeRequired = usuarioService.debeCambiarPassword(principal.name)
            )
            )
        )

    @PutMapping("/me/completar-datos")
    fun completarDatos(
        principal: Principal,
        @RequestBody dto: CompletarDatosUsuarioDTO,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val ip = request.getHeader("X-Forwarded-For")?.substringBefore(",")?.trim() ?: request.remoteAddr
        val u = usuarioService.completarDatos(principal.name, dto, ip, request.getHeader("User-Agent"))
        return ResponseEntity.ok(CustomResponse(message = "Datos completados", data = u.toUsuarioResponseDto()))
    }

    // ==================== BÚSQUEDAS ====================

    /**
     * Obtiene un usuario específico por su ID
     */
    @GetMapping("/{usuarioId}")
    fun getUsuarioById(
        @PathVariable usuarioId: Long
    ): ResponseEntity<CustomResponse<UsuarioResponseDto>> {

        val usuario = usuarioService.findById(usuarioId)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Usuario obtenido correctamente",
                data = usuario.toUsuarioResponseDto()
            )
        )
    }

    /**
     * Obtiene un usuario por su email
     * Busca tanto en usuarios como en clientes
     */
    @GetMapping("/email")
    fun getUsuarioByEmail(
        @RequestParam email: String
    ): ResponseEntity<CustomResponse<UsuarioResponseDto>> {
        val usuarioDto = usuarioService.getUsuarioDtoByEmail(email)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Usuario obtenido correctamente",
                data = usuarioDto
            )
        )
    }

    /**
     * Obtiene un usuario por su celular
     * Principalmente usado para búsqueda de clientes
     */
    @GetMapping("/celular")
    fun getUsuarioByCelular(
        @RequestParam celular: Long
    ): ResponseEntity<CustomResponse<UsuarioResponseDto>> {
        val usuarioDto = usuarioService.getUsuarioDtoByCelular(celular)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Usuario obtenido correctamente",
                data = usuarioDto
            )
        )
    }

    /**
     * Obtiene el perfil completo de un usuario por su ID
     */
    @GetMapping("/{usuarioId}/perfil")
    fun getPerfilUsuario(
        @PathVariable usuarioId: Long
    ): ResponseEntity<CustomResponse<UsuarioPerfilDTO?>> {
        val perfil = usuarioService.getUsuarioPerfil(usuarioId)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Perfil obtenido correctamente",
                data = perfil
            )
        )
    }

    /**
     * Obtiene la información de un usuario en el contexto de una empresa
     * @param usuarioId ID del usuario
     * @param empresaId ID de la empresa
     */
    @GetMapping("/{usuarioId}/empresa/{empresaId}")
    fun getUsuarioOfEmpresa(
        @PathVariable usuarioId: Long,
        @PathVariable empresaId: Long
    ): ResponseEntity<CustomResponse<UsuarioEditCargoDTO?>> {
        val usuarioOfEmpresa = usuarioService.getUsuarioOfEmpresa(usuarioId, empresaId)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Usuario de empresa obtenido correctamente",
                data = usuarioOfEmpresa
            )
        )
    }

    /**
     * Obtiene todas las empresas de un usuario
     */
    @GetMapping("/{usuarioId}/empresas")
    fun getEmpresas(
        @PathVariable usuarioId: Long
    ): ResponseEntity<CustomResponse<List<EmpresaAbmDTO>>> {
        val empresas = usuarioService.getAllEmpresaByUsuarioId(usuarioId)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Empresas obtenidas correctamente",
                data = empresas
            )
        )
    }

    // ==================== EMPLEADOS ====================

    /**
     * Obtiene todos los empleados (usuarios con cargo) de una empresa
     */
    @GetMapping("/empresa/{empresaId}")
    fun getEmpleados(
        @PathVariable empresaId: Long,
        @RequestParam(defaultValue = "0") page: Int
    ): ResponseEntity<CustomResponse<List<UsuarioAbmDTO>>> {
        val empleados = usuarioService.getAllUsuario(empresaId, page)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Empleados obtenidos correctamente",
                data = empleados
            )
        )
    }

    /**
     * Obtiene la cantidad total de empleados de una empresa
     */
    @GetMapping("/empresa/{empresaId}/cantidad")
    fun getCantidadEmpleados(
        @PathVariable empresaId: Long
    ): ResponseEntity<CustomResponse<Int>> {
        val cantidad = usuarioService.getCantidadUsuario(empresaId)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Cantidad obtenida correctamente",
                data = cantidad
            )
        )
    }

    /**
     * Obtiene empleados filtrados por nombre, apellido o username
     */
    @GetMapping("/empresa/{empresaId}/buscar/{buscar}")
    fun getEmpleadosFiltrados(
        @PathVariable empresaId: Long,
        @PathVariable buscar: String,
        @RequestParam(defaultValue = "0") page: Int
    ): ResponseEntity<CustomResponse<List<UsuarioAbmDTO>>> {
        val empleados = usuarioService.getAllUsuarioFiltrados(empresaId, page, buscar)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Empleados filtrados obtenidos correctamente",
                data = empleados
            )
        )
    }

    /**
     * Obtiene la cantidad de empleados filtrados
     */
    @GetMapping("/empresa/{empresaId}/buscar/{buscar}/cantidad")
    fun getCantidadEmpleadosFiltrados(
        @PathVariable empresaId: Long,
        @PathVariable buscar: String
    ): ResponseEntity<CustomResponse<Int>> {
        val cantidad = usuarioService.getCantidadFiltrados(empresaId, buscar)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Cantidad obtenida correctamente",
                data = cantidad
            )
        )
    }

    @PostMapping("/alta")
    fun alta(@RequestBody dto: AltaUsuarioDTO): ResponseEntity<CustomResponse<Long>> {
        val usuario = usuarioService.alta(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            CustomResponse(
                message = "Usuario creado correctamente",
                data = usuario.id
            )
        )
    }

    @GetMapping("/check-username")
    fun checkUsername(@RequestParam username: String): ResponseEntity<CustomResponse<Boolean>> {
        val isAvailable = !usuarioService.existsByUsername(UsernameNormalizer.normalize(username))
        return ResponseEntity.ok(
            CustomResponse(
                message = "Disponibilidad de username verificada",
                data = isAvailable
            )
        )
    }

    // ==================== CRUD ====================

    /**
     * Crea o actualiza un usuario
     * Si es nuevo (id == 0), encripta la contraseña
     * Si es actualización, mantiene la contraseña existente a menos que se cambie
     */
    @PostMapping
    fun saveUsuario(
        @RequestBody usuarioDto: UsuarioDTORequest
    ): ResponseEntity<CustomResponse<UsuarioResponseDto>> {
        val usuario = usuarioService.saveUsuario(usuarioDto)

        return ResponseEntity.status(HttpStatus.CREATED).body(
            CustomResponse(
                message = "Usuario guardado correctamente",
                data = usuario.toUsuarioResponseDto()
            )
        )
    }

    // ==================== ACTUALIZAR ====================

    /**
     * Actualiza la contraseña de un usuario
     */
    @PutMapping("/{usuarioId}/password")
    fun updatePassword(
        principal: Principal,
        @PathVariable usuarioId: Long,
        @RequestBody dto: UsuarioEditPasswordDTO
    ): ResponseEntity<CustomResponse<String>> {
        usuarioService.updatePassword(principal.name, usuarioId, dto.password)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Contraseña actualizada correctamente",
                data = "OK"
            )
        )
    }

    @PostMapping("/{usuarioId}/restablecer-password")
    fun resetPassword(
        principal: Principal,
        @PathVariable usuarioId: Long
    ): ResponseEntity<CustomResponse<String>> {
        usuarioService.restablecerPassword(principal.name, usuarioId)
        return ResponseEntity.ok(
            CustomResponse("Contraseña temporal generada y enviada correctamente", "OK")
        )
    }

    @PutMapping("/me/password")
    fun changeMyPassword(
        principal: Principal,
        @RequestBody dto: CambiarPasswordDTO
    ): ResponseEntity<CustomResponse<String>> {
        usuarioService.cambiarPassword(principal.name, dto.password)
        return ResponseEntity.ok(CustomResponse("Contraseña actualizada correctamente", "OK"))
    }

    // ==================== ELIMINAR ====================

    /**
     * Elimina un usuario (soft delete)
     */
    @DeleteMapping("/{usuarioId}")
    fun delete(
        @PathVariable usuarioId: Long
    ): ResponseEntity<CustomResponse<String>> {
        usuarioService.delete(usuarioId)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Usuario eliminado correctamente",
                data = "OK"
            )
        )
    }

}
