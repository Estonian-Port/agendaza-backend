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
data class CateringEventoExtraVariableCatering(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catering_evento_id")
    val cateringEvento: CateringEvento,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "extra_id")
    val extra: Extra,

    @Column
    val cantidad : Int) {}