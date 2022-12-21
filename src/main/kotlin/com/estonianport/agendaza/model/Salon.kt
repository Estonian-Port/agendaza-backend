package com.estonianport.agendaza.model

import com.fasterxml.jackson.annotation.JsonBackReference
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
data class Salon(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column
    val nombre: String,

    @Column
    val calle: String,

    @Column
    val numero: Int,

    @Column
    val municipio: String){

    @JsonBackReference
    @OneToMany(mappedBy = "salon")
    lateinit var listaEvento : Set<Evento>

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "listaTipoUsuarioTrabajo")
    val listaUsuario: MutableSet<Usuario> = mutableSetOf()

}