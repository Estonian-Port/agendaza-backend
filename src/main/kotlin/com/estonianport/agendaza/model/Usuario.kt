package com.estonianport.agendaza.model

import com.estonianport.agendaza.dto.ClienteDTO
import com.estonianport.agendaza.dto.UsuarioAbmDTO
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(
    name = "usuario",
    indexes = [
        Index(name = "idx_usuario_username", columnList = "username", unique = true),
        Index(name = "idx_usuario_email", columnList = "email", unique = true),
        Index(name = "idx_usuario_celular", columnList = "celular", unique = true)
    ]
)
class Usuario(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @Column
    var nombre: String,

    @Column
    var apellido: String,

    @Column
    var celular: Long,

    @Column
    var email: String) {

    @Column
    var username: String = ""

    @Column
    var password: String = ""

    @Column
    var fechaNacimiento: LocalDate = LocalDate.now()

    @Column
    var fechaAlta : LocalDate = LocalDate.now()

    @Column
    var fechaBaja : LocalDate? = null

    @JsonIgnore
    @OneToMany(mappedBy = "cliente", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var listaEventosContratados : MutableSet<Evento> = mutableSetOf()

    @JsonIgnore
    @OneToMany(mappedBy = "usuario", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var listaCargo: MutableSet<Cargo> = mutableSetOf()

    fun toUsuarioAbmDto(): UsuarioAbmDTO {
        return UsuarioAbmDTO(id, nombre, apellido, username)
    }

    fun toClienteDto(): ClienteDTO {
        return ClienteDTO(id, nombre, apellido, email, celular)
    }
}



