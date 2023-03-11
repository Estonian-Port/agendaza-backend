package com.estonianport.agendaza.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrimaryKeyJoinColumn

@Entity
data class EventoExtraVariableTipoEvento(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    val extra: Extra,

    @Column
    val cantidad : Int){

    @ManyToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    var agregados: Agregados = Agregados(0,0,0, mutableSetOf(), mutableSetOf())
}