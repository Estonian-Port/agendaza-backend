package com.estonianport.agendaza.model

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import java.sql.Date

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
abstract class Persona(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long,

    @Column
    private val nombre: String,

    @Column
    private val apellido: String,

    @Column
    private val cuil: Long,

    @Column
    private val celular: Long,

    @Column
    private val mail: String){}

@Entity
data class Usuario(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long,

    @Column
    private val username: String,

    @Column
    private val password: String) : Persona(0,"", "", 0, 0,""){

    @Column
    lateinit var tipoUsuario : TipoUsuario
}

@Entity
data class Cliente(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long,

    @JoinColumn(name = "sexo")
    private val sexo: Sexo,

    @Column(name = "fecha_nacimiento")
    private val fechaNacimiento: Date,

    @Column
    private val empresa: String,

    @Column
    private val ciudad: String,

    @Column
    private val provincia: String,

    @Column
    private val codigoPostal : Int,

    @JsonBackReference
    @OneToMany(mappedBy = "cliente")
    private val evento: Set<Evento>) : Persona(0,"", "", 0, 0,""){}