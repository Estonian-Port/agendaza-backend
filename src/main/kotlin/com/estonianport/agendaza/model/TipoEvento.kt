package com.estonianport.agendaza.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.CascadeType
import java.time.LocalTime
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.PrimaryKeyJoinColumn

@Entity
data class TipoEvento(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column
    val nombre: String,

    @JoinColumn(name = "duracion")
    @Enumerated(EnumType.STRING)
    val duracion : Duracion,

    @ManyToOne(cascade = [CascadeType.MERGE, CascadeType.PERSIST, CascadeType.PERSIST])
    @PrimaryKeyJoinColumn
    var capacidad: Capacidad,

    @Column
    val cantidadDuracion: LocalTime,

    @ManyToOne
    @PrimaryKeyJoinColumn
    val empresa: Empresa){

    @JsonIgnore
    @OneToMany(mappedBy = "tipoEvento")
    val listaPrecioConFecha: MutableSet<PrecioConFechaTipoEvento> = mutableSetOf()

    @JsonIgnore
    @ManyToMany(mappedBy = "listaTipoEvento")
    val listaServicio: MutableSet<Servicio> = mutableSetOf()

    @JsonIgnore
    @ManyToMany(mappedBy = "listaTipoEvento")
    val listaExtra: MutableSet<Extra> = mutableSetOf()

}