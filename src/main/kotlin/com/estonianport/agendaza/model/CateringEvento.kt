package com.estonianport.agendaza.model

import com.fasterxml.jackson.annotation.JsonIgnore
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
import jakarta.persistence.MapsId
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne

@Entity
data class CateringEvento(
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column
    val presupuesto : Int,

    @Column(name = "catering_otro")
    val canteringOtro : Int,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "catering_evento_tipo_catering",
        joinColumns = arrayOf(JoinColumn(name = "catering_evento_id")),
        inverseJoinColumns = arrayOf(JoinColumn(name = "extra_id"))
    )
    val listaTipoCatering: MutableSet<Extra>,

    @OneToMany(mappedBy = "cateringEvento", cascade = arrayOf(CascadeType.ALL))
    val listaCateringExtraVariableCatering: MutableSet<CateringEventoExtraVariableCatering>){

    @JsonIgnore
    @OneToOne(mappedBy = "catering", cascade = arrayOf(CascadeType.ALL))
    lateinit var evento : Evento
}