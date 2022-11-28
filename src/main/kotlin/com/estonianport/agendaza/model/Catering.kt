package com.estonianport.agendaza.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany

@Entity
data class Catering(
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long,

    @Column
    private val presupuesto : Int,

    @Column(name = "catering_otro")
    private val canteringOtro : Int,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "catering_tipo_catering",
        joinColumns = arrayOf(JoinColumn(name = "catering_id")),
        inverseJoinColumns = arrayOf(JoinColumn(name = "tipo_catering_id"))
    )
    private val listaTipoCatering: Set<TipoCatering>,

    @OneToMany(mappedBy = "catering", cascade = arrayOf(CascadeType.ALL))
    private val listaCateringExtraVariableCatering: Set<CateringExtraVariableCatering>){}