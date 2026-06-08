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
        Index(name = "idx_usuario_email_baja", columnList = "email,fecha_baja"),
        Index(name = "idx_usuario_celular_baja", columnList = "celular,fecha_baja")
    ]
)
class Usuario(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @Column(nullable = false)
    var nombre: String,

    @Column(nullable = false)
    var apellido: String,

    @Column(nullable = false)
    var celular: Long,

    @Column(nullable = false)
    var email: String) {

    @Column(nullable = true)
    var username: String? = null

    @Column(nullable = true)
    var password: String? = null

    @Column
    var fechaNacimiento: LocalDate = LocalDate.now()

    @Column(nullable = false, updatable = false)
    var fechaAlta: LocalDate = LocalDate.now()

    @Column
    var fechaBaja: LocalDate? = null

    @JsonIgnore
    @OneToMany(mappedBy = "usuario", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var listaCargo: MutableSet<Cargo> = mutableSetOf()

}
