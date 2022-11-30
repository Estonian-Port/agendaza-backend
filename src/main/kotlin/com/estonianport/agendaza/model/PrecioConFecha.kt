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

@Entity(name = "precio_con_fecha_extra_tipo_evento")
data class PrecioConFechaExtraTipoEvento(
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "extra_tipo_evento_id")
    val extraTipoEvento: Extra? = null) : PrecioConFecha(0, 0, LocalDateTime.now(), LocalDateTime.now(), Salon(0,"","",0,"")) {}

@Entity(name = "precio_con_fecha_extra_variable_catering")
data class PrecioConFechaExtraVariableCatering(
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "extra_variable_catering_id")
    val extraVariableCatering: Extra) : PrecioConFecha(0, 0, LocalDateTime.now(), LocalDateTime.now(), Salon(0,"","",0,"")) {}

@Entity(name = "precio_con_fecha_tipo_evento")
data class PrecioConFechaTipoEvento(
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_evento_id")
    val tipoEvento: TipoEvento) : PrecioConFecha(0, 0, LocalDateTime.now(), LocalDateTime.now(), Salon(0,"","",0,"")) {}


@Entity(name = "precio_con_fecha_tipo_catering")
data class PrecioConFechaTipoCatering(
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_catering_id")
    val tipoCatering: Extra) : PrecioConFecha(0, 0, LocalDateTime.now(), LocalDateTime.now(), Salon(0,"","",0,"")) {}

@Entity(name = "precio_con_fecha_extra_variable_tipo_evento")
data class PrecioConFechaExtraVariableTipoEvento(
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "extra_variable_tipo_evento_id")
    val extraVariableTipoEvento: Extra) : PrecioConFecha(0, 0, LocalDateTime.now(), LocalDateTime.now(), Salon(0,"","",0,"")) {}

