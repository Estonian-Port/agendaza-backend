package com.estonianport.agendaza.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany
import jakarta.persistence.PrimaryKeyJoinColumn
import java.sql.Date
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
    val cuil: Long,

    @Column
    val celular: Long,

    @Column
    val email: String,

    @PrimaryKeyJoinColumn
    @Enumerated(EnumType.STRING)
    val sexo: Sexo,

    @Column
    val username: String,

    @Column
    var password: String,

    @Column(name = "fecha_nacimiento")
    val fechaNacimiento: LocalDate) {

    @JsonIgnore
    @OneToMany(mappedBy = "cliente", cascade = arrayOf(CascadeType.ALL))
    val listaEventosContratados : MutableSet<Evento> = mutableSetOf()

    @JsonIgnore
    @OneToMany(mappedBy = "usuario", cascade = arrayOf(CascadeType.ALL))
    val listaCargo: MutableSet<Cargo> = mutableSetOf()

    @Column
    var habilitado : Boolean = true
}



