package com.estonianport.agendaza.model

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrimaryKeyJoinColumn

@Entity
data class Cargo(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @ManyToOne
    @PrimaryKeyJoinColumn
    val usuario: Usuario,

    @ManyToOne
    @PrimaryKeyJoinColumn
    val empresa: Empresa,

    @PrimaryKeyJoinColumn
    @Enumerated(EnumType.STRING)
    var tipoCargo : TipoCargo){}