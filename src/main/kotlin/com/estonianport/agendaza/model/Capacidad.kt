package com.estonianport.agendaza.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.hibernate.annotations.Proxy

@Entity
@Proxy(lazy = false)
open class Capacidad(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(name = "capacidad_adultos")
    var capacidadAdultos : Int,

    @Column(name = "capacidad_ninos")
    var capacidadNinos : Int){}