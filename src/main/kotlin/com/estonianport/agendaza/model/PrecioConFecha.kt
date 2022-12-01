package com.estonianport.agendaza.model

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import java.time.LocalDateTime

@MappedSuperclass
abstract class PrecioConFecha (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column
    val precio : Int,

    @Column
    val desde: LocalDateTime,

    @Column
    val hasta: LocalDateTime,

    @ManyToOne
    @JoinColumn(name = "salon_id")
    val salon: Salon){}

@Entity(name = "precio_con_fecha_extra")
data class PrecioConFechaExtra(
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "extra_id")
    val extra: Extra) : PrecioConFecha(0, 0, LocalDateTime.now(), LocalDateTime.now(), Salon(0,"","",0,"")) {}

@Entity(name = "precio_con_fecha_tipo_evento")
data class PrecioConFechaTipoEvento(

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_evento_id")
    val tipoEvento: TipoEvento) : PrecioConFecha(0, 0, LocalDateTime.now(), LocalDateTime.now(), Salon(0,"","",0,"")) {}

