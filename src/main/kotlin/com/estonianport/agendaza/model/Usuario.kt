package com.estonianport.agendaza.model

import com.estonianport.agendaza.dto.UsuarioAbmDto
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import java.time.LocalDate

@Entity
data class Usuario(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column
    val nombre: String,

    @Column
    val apellido: String,

    @Column
    val celular: Long,

    @Column
    val email: String) {

    @Column
    val username: String = ""

    @Column
    var password: String = ""

    @Column
    val fechaNacimiento: LocalDate = LocalDate.now()

    @Column
    val fechaAlta : LocalDate = LocalDate.now()

    @Column
    var fechaBaja : LocalDate? = null

    @JsonIgnore
    @OneToMany(mappedBy = "cliente", cascade = arrayOf(CascadeType.ALL), fetch = FetchType.LAZY)
    var listaEventosContratados : MutableSet<Evento> = mutableSetOf()

    @JsonIgnore
    @OneToMany(mappedBy = "usuario", cascade = arrayOf(CascadeType.ALL), fetch = FetchType.LAZY)
    var listaCargo: MutableSet<Cargo> = mutableSetOf()

    fun toUsuarioAbmDto(): UsuarioAbmDto {
        return UsuarioAbmDto(id, nombre, apellido, username)
    }
}



