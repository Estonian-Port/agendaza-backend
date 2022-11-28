package com.estonianport.agendaza.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
data class Salon(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column
    private val nombre: String,

    @Column
    private val calle: String,

    @Column
    private val numero: String,

    @Column
    private val municipio: String){
}