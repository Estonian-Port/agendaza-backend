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
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.OneToMany

@MappedSuperclass
abstract class Extra(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long,

    @Column
    private val nombre: String){}

@Entity
data class ExtraTipoEvento(

    @JsonManagedReference
    @ManyToMany(fetch = FetchType.LAZY, cascade = arrayOf(CascadeType.ALL))
    @JoinTable(
        name = "sub_tipo_evento_extra",
        joinColumns = arrayOf(JoinColumn(name = "extra_id")),
        inverseJoinColumns = arrayOf(JoinColumn(name = "sub_tipo_evento_id"))
    )
    private val listaTipoEvento: Set<TipoEvento>,

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "extraSubTipoEvento")
    private val listaPrecioConFecha: List<PrecioConFechaExtraSubTipoEvento>): Extra(0, "") {}

@Entity
data class ExtraVariableTipoEvento(
    @JsonManagedReference
    @ManyToMany(fetch = FetchType.LAZY, cascade = arrayOf(CascadeType.ALL))
    @JoinTable(
        name = "tipo_evento_extra_variable",
        joinColumns = arrayOf(JoinColumn(name = "extra_variable_id")),
        inverseJoinColumns = arrayOf(JoinColumn(name = "tipo_evento_id"))
    )
    private val listaTipoEvento: Set<TipoEvento>,

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "extraVariableSubTipoEvento")
    private val listaPrecioConFecha: List<PrecioConFechaExtraVariableTipoEvento>) : Extra(0, "") {}



@Entity(name = "tipo_catering")
data class TipoCatering(

    @JsonManagedReference
    @ManyToMany(fetch = FetchType.LAZY, cascade = arrayOf(CascadeType.ALL))
    @JoinTable(
        name = "tipo_evento_tipo_catering",
        joinColumns = arrayOf(JoinColumn(name = "tipo_catering_id")),
        inverseJoinColumns = arrayOf(JoinColumn(name = "tipo_evento_id"))
    )
    private val listaTipoEvento: Set<TipoEvento>,

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tipoCatering")
    private val listaPrecioConFecha: List<PrecioConFechaTipoCatering>) :  Extra(0, "") {}

@Entity
data class ExtraVariableCatering(
    @JsonManagedReference
    @ManyToMany(fetch = FetchType.LAZY, cascade = arrayOf(CascadeType.ALL))
    @JoinTable(
        name = "tipo_evento_extra_variable_catering",
        joinColumns = arrayOf(JoinColumn(name = "extra_variable_catering_id")),
        inverseJoinColumns = arrayOf(JoinColumn(name = "tipo_evento_id"))
    )
    private val listaTipoEvento: Set<TipoEvento>,

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "extraVariableCatering")
    private val listaPrecioConFecha: List<PrecioConFechaExtraVariableCatering>) : Extra(0, "") {
}