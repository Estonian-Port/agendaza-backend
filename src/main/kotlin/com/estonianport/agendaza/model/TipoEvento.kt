package com.estonianport.agendaza.model

import com.estonianport.agendaza.dto.TipoEventoDTO
import com.estonianport.agendaza.model.enums.Duracion
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
import jakarta.persistence.PrimaryKeyJoinColumn
import java.time.LocalDate

@Entity
class TipoEvento(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @Column
    var nombre: String,

    @JoinColumn(name = "duracion")
    @Enumerated(EnumType.STRING)
    var duracion : Duracion,

    @Column(name = "capacidad_adultos")
    var capacidadAdultos: Int,

    @Column(name = "capacidad_ninos")
    var capacidadNinos: Int,

    @Column
    var cantidadDuracion: LocalTime,

    @ManyToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    var empresa: Empresa){

    @ManyToMany(mappedBy = "listaTipoEvento", fetch = FetchType.LAZY)
    var listaExtra: MutableSet<Extra> = mutableSetOf()

    @ManyToMany(mappedBy = "listaTipoEvento", fetch = FetchType.LAZY)
    var listaServicio: MutableSet<Servicio> = mutableSetOf()

    @Column
    var fechaBaja : LocalDate? = null
}