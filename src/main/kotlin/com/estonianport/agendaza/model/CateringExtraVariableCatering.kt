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
data class CateringExtraVariableCatering(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catering_id")
    private val catering: Catering,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "extra_variable_catering_id")
    private val extraVariableCatering: ExtraVariableCatering,

    @Column
    private val cantidad : Int) {}