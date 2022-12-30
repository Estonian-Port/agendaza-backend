package com.estonianport.agendaza.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.time.LocalDateTime

@Entity
data class Pago (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column
    val monto : Int,

    @ManyToOne
    @JoinColumn(name = "medio_de_pago_id")
    val medioDePago: MedioDePago,

    @Column
    val fecha: LocalDateTime,

    @ManyToOne
    @JoinColumn(name = "evento_id")
    val evento: Evento,

    @ManyToOne
    @JoinColumn(name = "encargado_id")
    val encargado: Usuario){}