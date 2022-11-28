package com.estonianport.agendaza.model

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.Entity
import java.sql.Date
import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany

@Entity
data class Cliente(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long,

    @Column
    private val nombre: String,

    @Column
    private val apellido: String,

    @JoinColumn(name = "sexo")
    private val sexo: Sexo,

    @Column
    private val cuil: Long,

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

    @Column
    private val email: String,

    @Column
    private val celular: Long,

    @JsonBackReference
    @OneToMany(mappedBy = "cliente")
    private val evento: Set<Evento>){}