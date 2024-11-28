package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.TipoCargo
import com.estonianport.agendaza.model.Usuario
import java.time.LocalDate

class UsuarioDTO(var usuario: Usuario, var empresaId : Long, var cargo : TipoCargo){}
class UsuarioPerfilDTO(val id: Long, var nombre: String, val apellido: String, val username: String, val email: String, val celular : Long, val fechaNacimiento : LocalDate)

class UsuarioEditCargoDTO(var id : Long, var empresaId : Long, var cargo : TipoCargo){}

class UsuarioEditPasswordDTO(var id : Long, var password: String) {}

class UsuarioAbmDTO(var id: Long, var nombre: String, var apellido: String, var username: String) {}

class UsuarioEmpresaDTO(var usuarioId: Long, var empresaId: Long) {}
