package com.estonianport.agendaza.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
data class Capacidad(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long,

    @Column(name = "capacidad_adultos")
    private val capacidadAdultos : Int,

    @Column(name = "capacidad_ninos")
    private val capacidadNinos : Int){}