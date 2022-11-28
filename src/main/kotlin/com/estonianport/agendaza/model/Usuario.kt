package com.estonianport.agendaza.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
data class Usuario(
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long,

    @Column
    private val nombre: String,

    @Column
    private val apellido: String,

    @Column
    private val username: String,

    @Column
    private val password: String,

    @Column
    private val mail: String){}