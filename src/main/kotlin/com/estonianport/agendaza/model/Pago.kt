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
    private val id: Long,

    @Column
    private val monto : Int,

    @ManyToOne
    @JoinColumn(name = "medio_de_pago_id")
    private val medioDePago: MedioDePago,

    @Column
    private val fecha: LocalDateTime,

    @ManyToOne
    @JoinColumn(name = "evento_id")
    private val evento: Evento,

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private val usuario: Usuario){}