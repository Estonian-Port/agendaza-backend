package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.Usuario
import com.estonianport.agendaza.model.enums.TipoCargo
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

data class UsuarioDTORequest(
    var usuario: Usuario,
    var empresaId: Long,
    var cargo: TipoCargo?
) : Serializable

data class AltaUsuarioDTO(
    val email: String,
    val empresaId: Long,
    val rol: TipoCargo
) : Serializable

data class CompletarDatosUsuarioDTO(
    val nombre: String,
    val apellido: String,
    val celular: Long,
    val fechaNacimiento: LocalDate,
    val username: String,
    val password: String,
    val email: String,
    val aceptoTerminos: Boolean,
    val versionTerminos: String
) : Serializable

data class UsuarioPerfilDTO(
    val id: Long,
    var nombre: String,
    val apellido: String,
    val username: String,
    val email: String,
    val celular: Long,
    val fechaNacimiento: LocalDate
) : Serializable

data class ClienteDTO(
    val id: Long,
    var nombre: String,
    val apellido: String,
    val email: String,
    val celular: Long
) : Serializable

data class UsuarioEditCargoDTO(
    var id: Long,
    var empresaId: Long,
    var cargo: TipoCargo
) : Serializable

data class UsuarioEditPasswordDTO(
    var id: Long,
    var password: String
) : Serializable

data class UsuarioAbmDTO(
    var id: Long,
    var nombre: String,
    var apellido: String,
    var username: String?
) : Serializable

data class UsuarioResponseDto(
    val id: Long,
    val nombre: String,
    val apellido: String,
    val username: String?,
    val email: String,
    val celular: Long,
    val fechaNacimiento: LocalDate
) : Serializable

// Funciones de extensión más idiomáticas (expression body)
fun Usuario.toUsuarioResponseDto() = UsuarioResponseDto(
    id = this.id,
    nombre = this.nombre,
    apellido = this.apellido,
    username = this.username,
    email = this.email,
    celular = this.celular,
    fechaNacimiento = this.fechaNacimiento
)

fun Usuario.toUsuarioAbmDto() = UsuarioAbmDTO(
    id = this.id,
    nombre = this.nombre,
    apellido = this.apellido,
    username = this.username
)

fun CompletarDatosUsuarioDTO.updateUsuario(usuario: Usuario, encodedPassword: String): Usuario = usuario.apply {
    nombre = this@updateUsuario.nombre
    apellido = this@updateUsuario.apellido
    celular = this@updateUsuario.celular
    fechaNacimiento = this@updateUsuario.fechaNacimiento
    username = this@updateUsuario.username
    email = this@updateUsuario.email
    password = encodedPassword
    fechaCambioPassword = LocalDateTime.now()

}