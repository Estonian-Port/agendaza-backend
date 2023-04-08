package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.TipoCargo
import com.estonianport.agendaza.model.Usuario

class UsuarioDto(var usuario: Usuario, var empresaId : Long, var rol : TipoCargo){}

class UsuarioEditPasswordDto(var id : Long, var password: String) {}

class UsuarioAbmDto(var id: Long, var nombre: String, var apellido: String, var username: String) {}

class UsuarioEmpresaDto(var usuarioId: Long, var empresaId: Long) {}

