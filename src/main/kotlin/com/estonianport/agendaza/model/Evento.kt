package com.estonianport.agendaza.model

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
import jakarta.persistence.OneToMany
import java.time.LocalDateTime

@Entity
data class Evento(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long,
    @Column
    private val nombre: String,

    @ManyToOne
    @JoinColumn(name = "salon_id")
    private val salon: Salon,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_tipo_evento_id")
    private val subTipoEvento: SubTipoEvento,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "evento_extra_sub_tipo_evento",
        joinColumns = arrayOf(JoinColumn(name = "evento_id")),
        inverseJoinColumns = arrayOf(JoinColumn(name = "extra_sub_tipo_evento_id"))
    )
    private val listaExtraSubTipoEvento: Set<ExtraSubTipoEvento>,

    @OneToMany(mappedBy = "evento", cascade = arrayOf(CascadeType.ALL))
    private val listaEventoExtraVariable: Set<EventoExtraVariableSubTipoEvento>,

    @Column
    private val startd: LocalDateTime,

    @Column
    private val endd: LocalDateTime,

    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "capacidad_id")
    private val capacidad: Capacidad,

    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "catering_id")
    private val catering: Catering,

    @Column
    private val presupuesto: Int,

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private val usuario: Usuario,

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private val cliente: Cliente,

    @Column
    private val codigo: String,

    @Column
    private val extraOtro: Int,

    @Column
    private val descuento : Int){
}