package com.estonianport.agendaza.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrimaryKeyJoinColumn

@Entity
class EventoExtraVariable(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @ManyToOne
    @PrimaryKeyJoinColumn
    var extra: Extra,

    @Column
    var cantidad : Int) {

    @ManyToOne
    @PrimaryKeyJoinColumn
    lateinit var evento: Evento

}
