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
    var id: Long,

    @Column
    open var precio : Int,

    @Column
    open var desde: LocalDateTime,

    @Column
    open var hasta: LocalDateTime,

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    open var empresa: Empresa){
}

@Entity(name = "precio_con_fecha_extra")
class PrecioConFechaExtra(
    id: Long,
    precio: Int,
    desde: LocalDateTime,
    hasta: LocalDateTime,
    empresa: Empresa,

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "extra_id")
    val extra: Extra

    ) : PrecioConFecha(id, precio, desde, hasta, empresa) {}

@Entity(name = "precio_con_fecha_tipo_evento")
class PrecioConFechaTipoEvento(
    id: Long,
    precio: Int,
    desde: LocalDateTime,
    hasta: LocalDateTime,
    empresa: Empresa,

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_evento_id")
    val tipoEvento: TipoEvento): PrecioConFecha(id, precio, desde, hasta, empresa) {}

