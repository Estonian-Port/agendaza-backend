package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.enums.TipoCargo
import com.estonianport.agendaza.model.Usuario
import java.io.Serializable
import java.time.LocalDate

class UsuarioDTO(var usuario: Usuario, var empresaId : Long, var cargo : TipoCargo?){}

class UsuarioPerfilDTO(val id: Long, var nombre: String, val apellido: String, val username: String, val email: String, val celular : Long, val fechaNacimiento : LocalDate)

class ClienteDTO(val id: Long, var nombre: String, val apellido: String, val email: String, val celular : Long)

class UsuarioEditCargoDTO(var id : Long, var empresaId : Long, var cargo : TipoCargo){}

class UsuarioEditPasswordDTO(var id : Long, var password: String) {}

class UsuarioAbmDTO(var id: Long, var nombre: String, var apellido: String, var username: String?) {}

data class UsuarioResponseDto(
    val id: Long,
    val nombre: String,
    val apellido: String,
    val username: String?,
    val email: String,
    val celular: Long
) : Serializable

fun Usuario.toUsuarioResponseDto(): UsuarioResponseDto {
    return UsuarioResponseDto(
        id = this.id,
        nombre = this.nombre,
        apellido = this.apellido,
        username = this.username,
        email = this.email,
        celular = this.celular
    )
}

fun Usuario.toUsuarioAbmDto(): UsuarioAbmDTO {
    return UsuarioAbmDTO(id, nombre, apellido, username)
}