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
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrimaryKeyJoinColumn

@Entity
data class Servicio(
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @Column
    var nombre: String,

    @ManyToOne(cascade = [CascadeType.PERSIST])
    @PrimaryKeyJoinColumn
    val empresa: Empresa){

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "tipo_evento_servicio",
        joinColumns = arrayOf(JoinColumn(name = "servicio_id")),
        inverseJoinColumns = arrayOf(JoinColumn(name = "tipo_evento_id"))
    )
    var listaTipoEvento: MutableSet<TipoEvento> = mutableSetOf()

}