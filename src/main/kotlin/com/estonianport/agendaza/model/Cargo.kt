package com.estonianport.agendaza.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrimaryKeyJoinColumn

@Entity
data class Cargo(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @ManyToOne(fetch = FetchType.EAGER)
    @PrimaryKeyJoinColumn
    val usuario: Usuario,

    @ManyToOne(fetch = FetchType.EAGER)
    @PrimaryKeyJoinColumn
    val empresa: Empresa,

    @Column
    @Enumerated(EnumType.STRING)
    val tipoCargo : TipoCargo){}