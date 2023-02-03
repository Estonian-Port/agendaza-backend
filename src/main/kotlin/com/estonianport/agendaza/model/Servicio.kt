package com.estonianport.agendaza.model

import com.fasterxml.jackson.annotation.JsonBackReference
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
    val id: Long,

    @Column
    val nombre: String){

    @JsonBackReference
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "tipo_evento_servicio",
        joinColumns = arrayOf(JoinColumn(name = "servicio_id")),
        inverseJoinColumns = arrayOf(JoinColumn(name = "tipo_evento_id"))
    )
    val listaTipoEvento: MutableSet<TipoEvento> = mutableSetOf()

    @JsonBackReference
    @ManyToMany
    @JoinTable(
        name = "empresa_servicio",
        joinColumns = arrayOf(JoinColumn(name = "servicio_id")),
        inverseJoinColumns = arrayOf(JoinColumn(name = "empresa_id"))
    )
    val listaEmpresa: MutableSet<Empresa> = mutableSetOf()
}