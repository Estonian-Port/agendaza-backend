package com.estonianport.agendaza.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
data class EventoExtraVariableTipoEvento(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id")
    val evento: Evento,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "extra_variable_sub_tipo_evento_id")
    val extraVariableTipoEvento: ExtraVariableTipoEvento,

    @Column
    val cantidad : Int){}