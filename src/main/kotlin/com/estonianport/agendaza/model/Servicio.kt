package com.estonianport.agendaza.model

import com.fasterxml.jackson.annotation.JsonManagedReference
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

@Entity
data class Servicio(
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long,

    @Column
    private val nombre: String,

    @JsonManagedReference
    @ManyToMany(fetch = FetchType.LAZY, cascade = arrayOf(CascadeType.ALL))
    @JoinTable(
        name = "sub_tipo_evento_servicio",
        joinColumns = arrayOf(JoinColumn(name = "servicio_id")),
        inverseJoinColumns = arrayOf(JoinColumn(name = "sub_tipo_evento_id"))
    )
    private val listaSubTipoEvento: Set<SubTipoEvento>){}