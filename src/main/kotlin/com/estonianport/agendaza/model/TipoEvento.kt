package com.estonianport.agendaza.model

import com.fasterxml.jackson.annotation.JsonBackReference
import java.time.LocalTime
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany

@Entity
data class TipoEvento(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column
    val nombre: String,

    @JoinColumn(name = "duracion")
    val duracion : Duracion,

    @ManyToOne
    @JoinColumn(name = "capacidad_id")
    var capacidad: Capacidad,

    @Column
    val cantidadDuracion: LocalTime,

    @Column(name = "cant_personal")
    val cantPersonal : Int,

    @Column(name = "valor_fin_semana")
    val valorFinSemana : Int){

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tipoEvento")
    val listaPrecioConFecha: List<PrecioConFechaTipoEvento> = mutableListOf()

    @JsonBackReference
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "listaTipoEvento")
    val listaServicio: MutableSet<Servicio> = mutableSetOf()

    @JsonBackReference
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "listaTipoEvento")
    val listaExtra: MutableSet<Extra> = mutableSetOf()
}