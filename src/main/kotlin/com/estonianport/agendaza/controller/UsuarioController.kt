package com.estonianport.agendaza.controller

import com.estonianport.agendaza.dto.*
import com.estonianport.agendaza.dto.response.CustomResponse
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.Cargo
import com.estonianport.agendaza.model.enums.TipoCargo
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
     * Obtiene el usuario logueado desde el token JWT
     */
    @GetMapping("/me")
    fun getUsuarioLogueado(principal: Principal): ResponseEntity<CustomResponse<UsuarioResponseDto>> {
        val usuarioDto = usuarioService.getUsuarioDtoByEmail(principal.name)
        return ResponseEntity.ok(
            CustomResponse(
                message = "Usuario obtenido correctamente",
                data = usuarioDto
            )
        )
    }

    /**
     * Obtiene el perfil del usuario logueado
     */
    @GetMapping("/perfil/me")
    fun getPerfilLogueado(principal: Principal): ResponseEntity<CustomResponse<UsuarioPerfilDTO>> {
        val usuario = usuarioService.getByEmail(principal.name)
            ?: throw NotFoundException("Usuario no encontrado")

        val perfil = usuarioService.getUsuarioPerfil(usuario.id)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Perfil obtenido correctamente",
                data = perfil
            )
        )
    }

    /**
     * Obtiene un usuario por email
     */
    @GetMapping("/email/{email}")
    fun getUsuarioByEmail(@PathVariable email: String): ResponseEntity<CustomResponse<UsuarioResponseDto>> {
        val usuarioDto = usuarioService.getUsuarioDtoByEmail(email)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Usuario obtenido correctamente",
                data = usuarioDto
            )
        )
    }

    /**
     * Obtiene un usuario por celular
     */
    @GetMapping("/celular/{celular}")
    fun getUsuarioByCelular(@PathVariable celular: Long): ResponseEntity<CustomResponse<Usuario>> {
        val usuario = usuarioService.getByCelular(celular)
            ?: throw NotFoundException("Cliente no encontrado")

        return ResponseEntity.ok(
            CustomResponse(
                message = "Usuario obtenido correctamente",
                data = usuario
            )
        )
    }

    // ==================== EMPLEADOS ====================

    /**
     * Obtiene todos los empleados de una empresa
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
     * Obtiene la cantidad de empleados de una empresa
     */
    @GetMapping("/empresa/{empresaId}/cantidad")
    fun getCantidadEmpleados(@PathVariable empresaId: Long): ResponseEntity<CustomResponse<Int>> {
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
     * Obtiene todos los clientes de una empresa
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
     * Obtiene la cantidad de clientes
     */
    @GetMapping("/empresa/{empresaId}/clientes/cantidad")
    fun getCantidadClientes(@PathVariable empresaId: Long): ResponseEntity<CustomResponse<Int>> {
        val cantidad = usuarioService.getCantidadCliente(empresaId)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Cantidad obtenida correctamente",
                data = cantidad
            )
        )
    }

    /**
     * Obtiene clientes filtrados
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

    // ==================== EMPRESAS ====================

    /**
     * Obtiene todas las empresas de un usuario
     */
    @GetMapping("/{usuarioId}/empresas")
    fun getEmpresas(@PathVariable usuarioId: Long): ResponseEntity<CustomResponse<List<EmpresaAbmDTO>>> {
        val empresas = usuarioService.getAllEmpresaByUsuarioId(usuarioId)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Empresas obtenidas correctamente",
                data = empresas
            )
        )
    }

    // ==================== CRUD ====================

    /**
     * Crea o actualiza un usuario
     */
    @PostMapping
    fun saveUsuario(@RequestBody usuarioDto: UsuarioDTO): ResponseEntity<CustomResponse<Usuario>> {
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

        return ResponseEntity.status(HttpStatus.CREATED).body(
            CustomResponse(
                message = "Usuario guardado correctamente",
                data = usuario
            )
        )
    }

    /**
     * Crea un cliente
     */
    @PostMapping("/clientes")
    fun saveCliente(@RequestBody clienteDto: ClienteDTO): ResponseEntity<CustomResponse<ClienteDTO>> {
        val usuario = Usuario(clienteDto.id, clienteDto.nombre, clienteDto.apellido, clienteDto.celular, clienteDto.email)
        val usuarioGuardado = usuarioService.save(usuario)

        return ResponseEntity.status(HttpStatus.CREATED).body(
            CustomResponse(
                message = "Cliente guardado correctamente",
                data = usuarioGuardado.toClienteDto()
            )
        )
    }

    // ==================== ACTUALIZAR INFORMACIÓN ====================

    /**
     * Actualiza el cargo de un usuario en una empresa
     */
    @PutMapping("/{usuarioId}/cargo")
    fun updateCargo(
        @PathVariable usuarioId: Long,
        @RequestBody usuarioEditCargoDTO: UsuarioEditCargoDTO
    ): ResponseEntity<CustomResponse<Long>> {
        val cargo = cargoService.getCargoByEmpresaIdAndUsuarioId(usuarioEditCargoDTO.empresaId, usuarioId)
        cargo.tipoCargo = usuarioEditCargoDTO.cargo
        val cargoGuardado = cargoService.save(cargo)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Cargo actualizado correctamente",
                data = cargoGuardado.id
            )
        )
    }

    /**
     * Actualiza la contraseña de un usuario
     */
    @PutMapping("/{usuarioId}/password")
    fun updatePassword(
        @PathVariable usuarioId: Long,
        @RequestBody usuarioEditPasswordDTO: UsuarioEditPasswordDTO
    ): ResponseEntity<CustomResponse<String>> {
        val usuario = usuarioService.findById(usuarioId)
            ?: throw NotFoundException("Usuario no encontrado")

        usuario.password = passwordEncoder.encode(usuarioEditPasswordDTO.password)
        usuarioService.save(usuario)

        return ResponseEntity.ok(
            CustomResponse(
                message = "Contraseña actualizada correctamente",
                data = "OK"
            )
        )
    }

    // ==================== ELIMINAR ====================

    /**
     * Elimina un cargo de un usuario en una empresa
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
