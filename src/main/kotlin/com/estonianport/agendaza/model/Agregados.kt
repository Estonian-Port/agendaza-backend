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
data class Agregados(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column
    var extraOtro: Long,

    @Column
    var descuento : Long,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "agregados_extra_tipo_evento",
        joinColumns = arrayOf(JoinColumn(name = "agregados_id")),
        inverseJoinColumns = arrayOf(JoinColumn(name = "extra_id"))
    )
    var listaExtra: MutableSet<Extra>,

    @OneToMany(mappedBy = "agregados")
    var listaEventoExtraVariable: MutableSet<EventoExtraVariableTipoEvento>) {

    @JsonIgnore
    @OneToOne(mappedBy = "agregados", cascade = arrayOf(CascadeType.ALL))
    lateinit var evento : Evento
}