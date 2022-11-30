package com.estonianport.agendaza.model

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.PrimaryKeyJoinColumn
import java.sql.Date

@MappedSuperclass
abstract class Persona(

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
    val mail: String){}

@Entity
data class Usuario(

    @Column
    val username: String,

    @Column
    val password: String) : Persona(0,"", "", 0, 0,""){

    @OneToOne
    lateinit var tipoUsuario : TipoUsuario
}

@Entity
data class Cliente(

    @PrimaryKeyJoinColumn
    val sexo: Sexo,

    @Column(name = "fecha_nacimiento")
    val fechaNacimiento: Date,

    @Column
    val empresa: String,

    @Column
    val ciudad: String,

    @Column
    val provincia: String,

    @Column
    val codigoPostal : Int,

    @JsonBackReference
    @OneToMany(mappedBy = "cliente")
    val evento: Set<Evento>) : Persona(0,"", "", 0, 0,""){}