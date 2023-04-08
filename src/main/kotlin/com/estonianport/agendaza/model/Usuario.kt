package com.estonianport.agendaza.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
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

    @Column(name = "fecha_nacimiento")
    val fechaNacimiento: LocalDate = LocalDate.now()

    @JsonIgnore
    @OneToMany(mappedBy = "cliente", cascade = arrayOf(CascadeType.ALL))
    var listaEventosContratados : MutableSet<Evento> = mutableSetOf()

    @JsonIgnore
    @OneToMany(mappedBy = "usuario", cascade = arrayOf(CascadeType.ALL))
    var listaCargo: MutableSet<Cargo> = mutableSetOf()

    @Column
    var habilitado : Boolean = true
}



