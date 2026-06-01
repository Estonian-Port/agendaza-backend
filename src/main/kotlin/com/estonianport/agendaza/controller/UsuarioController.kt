package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.*
import com.estonianport.agendaza.dto.response.CustomResponse
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.Cargo
import com.estonianport.agendaza.model.Usuario
import com.estonianport.agendaza.service.CargoService
import com.estonianport.agendaza.service.EmpresaService
import com.estonianport.agendaza.service.UsuarioService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*
import java.security.Principal

/**
 * Controlador para gestión de usuarios
 * Maneja CRUD, búsquedas, filtrados y relaciones empresa-usuario
 */
@RestController
@RequestMapping("/v1/usuarios")
@CrossOrigin("*")
class UsuarioController {

    @Autowired
    lateinit var usuarioService: UsuarioService

    @Autowired
    lateinit var empresaService: EmpresaService

    @Autowired
    lateinit var cargoService: CargoService

    private val passwordEncoder = BCryptPasswordEncoder()

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

    // ==================== BÚSQUEDAS ====================

    /**
     * Obtiene un usuario específico por su ID
     */
    @GetMapping("/{usuarioId}")
    fun getUsuarioById(
        @PathVariable usuarioId: Long
    ): ResponseEntity<CustomResponse<UsuarioResponseDto>> {

        val usuario = usuarioService.findById(usuarioId)
            ?: throw NotFoundException("Usuario con ID $usuarioId no encontrado")

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
            ?: throw NotFoundException("Usuario con email '$email' no encontrado")

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
        val usuario = usuarioService.getByCelular(celular)
            ?: throw NotFoundException("Usuario con celular '$celular' no encontrado")

        val usuarioDto = usuario.run {
            UsuarioResponseDto(id, nombre, apellido, username, email, celular)
        }

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
    ): ResponseEntity<CustomResponse<UsuarioPerfilDTO>> {
        val perfil = usuarioService.getUsuarioPerfil(usuarioId)
            ?: throw NotFoundException("Usuario con ID $usuarioId no encontrado")

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
    ): ResponseEntity<CustomResponse<UsuarioEditCargoDTO>> {
        val usuarioOfEmpresa = usuarioService.getUsuarioOfEmpresa(usuarioId, empresaId)
            ?: throw NotFoundException("Usuario no encontrado en esta empresa")

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

    // ==================== CLIENTES ====================

    /**
     * Obtiene todos los clientes (usuarios sin cargo) de una empresa
     */
    @GetMapping("/empresa/{empresaId}/clientes")
    fun getClientes(
        @PathVariable empresaId: Long,
        @RequestParam(defaultValue = "0") page: Int
    ): ResponseEntity<CustomResponse<List<UsuarioAbmDTO>>> {
        val clientes = usuarioService.getAllCliente(empresaId, page)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Clientes obtenidos correctamente",
                data = clientes
            )
        )
    }

    /**
     * Obtiene la cantidad total de clientes de una empresa
     */
    @GetMapping("/empresa/{empresaId}/clientes/cantidad")
    fun getCantidadClientes(
        @PathVariable empresaId: Long
    ): ResponseEntity<CustomResponse<Int>> {
        val cantidad = usuarioService.getCantidadCliente(empresaId)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Cantidad obtenida correctamente",
                data = cantidad
            )
        )
    }

    /**
     * Obtiene clientes filtrados por nombre o apellido
     */
    @GetMapping("/empresa/{empresaId}/clientes/buscar/{buscar}")
    fun getClientesFiltrados(
        @PathVariable empresaId: Long,
        @PathVariable buscar: String,
        @RequestParam(defaultValue = "0") page: Int
    ): ResponseEntity<CustomResponse<List<UsuarioAbmDTO>>> {
        val clientes = usuarioService.getAllClienteFiltrados(empresaId, page, buscar)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Clientes filtrados obtenidos correctamente",
                data = clientes
            )
        )
    }

    /**
     * Obtiene la cantidad de clientes filtrados
     */
    @GetMapping("/empresa/{empresaId}/clientes/buscar/{buscar}/cantidad")
    fun getCantidadClientesFiltrados(
        @PathVariable empresaId: Long,
        @PathVariable buscar: String
    ): ResponseEntity<CustomResponse<Int>> {
        val cantidad = usuarioService.getCantidadClienteFiltrados(empresaId, buscar)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Cantidad obtenida correctamente",
                data = cantidad
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
        @RequestBody usuarioDto: UsuarioDTO
    ): ResponseEntity<CustomResponse<UsuarioResponseDto>> {
        // Si es nuevo, encriptar contraseña
        if (usuarioDto.usuario.id == 0L) {
            usuarioDto.usuario.password = passwordEncoder.encode(usuarioDto.usuario.password)
        } else {
            // Si es actualización, mantener la contraseña existente
            val usuarioExistente = usuarioService.findById(usuarioDto.usuario.id)
            usuarioDto.usuario.password = usuarioExistente?.password ?: ""
        }

        val usuario = usuarioService.save(usuarioDto.usuario)

        // Asignar cargo si se proporciona
        if (usuarioDto.empresaId != 0L && usuarioDto.cargo != null) {
            val empresa = empresaService.get(usuarioDto.empresaId)
            if (empresa != null) {
                val cargoExistente = empresa.listaEmpleados.find { it.usuario.id == usuario.id }
                if (cargoExistente != null) {
                    cargoExistente.tipoCargo = usuarioDto.cargo!!
                    cargoService.save(cargoExistente)
                } else {
                    cargoService.save(Cargo(0, usuario, empresa, usuarioDto.cargo!!))
                }
            }
        }

        val responseDto = UsuarioResponseDto(
            usuario.id, usuario.nombre, usuario.apellido, usuario.username, usuario.email, usuario.celular
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(
            CustomResponse(
                message = "Usuario guardado correctamente",
                data = responseDto
            )
        )
    }

    /**
     * Crea un nuevo cliente
     */
    @PostMapping("/clientes")
    fun saveCliente(
        @RequestBody clienteDto: ClienteDTO
    ): ResponseEntity<CustomResponse<UsuarioResponseDto>> {
        val usuario = Usuario(
            clienteDto.id, clienteDto.nombre, clienteDto.apellido,
            clienteDto.celular, clienteDto.email
        )
        val usuarioGuardado = usuarioService.save(usuario)

        val responseDto = UsuarioResponseDto(
            usuarioGuardado.id, usuarioGuardado.nombre, usuarioGuardado.apellido,
            usuarioGuardado.username, usuarioGuardado.email, usuarioGuardado.celular
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(
            CustomResponse(
                message = "Cliente guardado correctamente",
                data = responseDto
            )
        )
    }

    // ==================== ACTUALIZAR ====================

    /**
     * Actualiza la contraseña de un usuario
     */
    @PutMapping("/{usuarioId}/password")
    fun updatePassword(
        @PathVariable usuarioId: Long,
        @RequestBody dto: UsuarioEditPasswordDTO
    ): ResponseEntity<CustomResponse<String>> {
        val usuario = usuarioService.findById(usuarioId)
            ?: throw NotFoundException("Usuario no encontrado")

        usuario.password = passwordEncoder.encode(dto.password)
        usuarioService.save(usuario)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Contraseña actualizada correctamente",
                data = "OK"
            )
        )
    }

    /**
     * Actualiza el cargo de un usuario en una empresa
     * Se delegó a CargoController pero se mantiene aquí por compatibilidad
     */
    @PutMapping("/{usuarioId}/cargo")
    fun updateCargo(
        @PathVariable usuarioId: Long,
        @RequestBody usuarioEditCargoDTO: UsuarioEditCargoDTO
    ): ResponseEntity<CustomResponse<Long>> {
        val cargo = cargoService.getCargoByEmpresaIdAndUsuarioId(
            usuarioEditCargoDTO.empresaId, usuarioId
        )
        cargo.tipoCargo = usuarioEditCargoDTO.cargo
        val cargoGuardado = cargoService.save(cargo)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Cargo actualizado correctamente",
                data = cargoGuardado.id
            )
        )
    }

    // ==================== ELIMINAR ====================

    /**
     * Elimina un usuario (soft delete)
     */
    @DeleteMapping("/{usuarioId}")
    fun deleteUsuario(
        @PathVariable usuarioId: Long
    ): ResponseEntity<CustomResponse<String>> {
        val usuario = usuarioService.findById(usuarioId)
            ?: throw NotFoundException("Usuario no encontrado")

        usuarioService.delete(usuario.id)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Usuario eliminado correctamente",
                data = "OK"
            )
        )
    }

    /**
     * Elimina el cargo de un usuario en una empresa
     * Se delegó a CargoController pero se mantiene aquí por compatibilidad
     */
    @DeleteMapping("/{usuarioId}/cargo/{empresaId}")
    fun deleteCargo(
        @PathVariable usuarioId: Long,
        @PathVariable empresaId: Long
    ): ResponseEntity<CustomResponse<String>> {
        val cargo = cargoService.getCargoByEmpresaIdAndUsuarioId(empresaId, usuarioId)
        cargoService.delete(cargo.id)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Cargo eliminado correctamente",
                data = "OK"
            )
        )
    }
}
