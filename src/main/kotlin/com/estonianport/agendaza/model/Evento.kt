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
    val id: Long,
    @Column
    val nombre: String,

    @ManyToOne
    @JoinColumn(name = "salon_id")
    val salon: Salon,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_evento_id")
    val tipoEvento: TipoEvento,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "evento_extra_tipo_evento",
        joinColumns = arrayOf(JoinColumn(name = "evento_id")),
        inverseJoinColumns = arrayOf(JoinColumn(name = "extra_tipo_evento_id"))
    )
    val listaExtraTipoEvento: Set<ExtraTipoEvento>,

    @OneToMany(mappedBy = "evento", cascade = arrayOf(CascadeType.ALL))
    val listaEventoExtraVariable: Set<EventoExtraVariableTipoEvento>,

    @Column
    val startd: LocalDateTime,

    @Column
    val endd: LocalDateTime,

    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "capacidad_id")
    val capacidad: Capacidad,

    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "catering_id")
    val catering: Catering,

    @Column
    val presupuesto: Int,

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    val usuario: Usuario,

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    val cliente: Cliente,

    @Column
    val codigo: String,

    @Column
    val estado: Estado,

    @Column
    val extraOtro: Int,

    @Column
    val descuento : Int){
}