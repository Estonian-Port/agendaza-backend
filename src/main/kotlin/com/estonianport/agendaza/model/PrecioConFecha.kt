package com.estonianport.agendaza.model

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PrimaryKeyJoinColumn
import java.time.LocalDateTime

@MappedSuperclass
abstract class PrecioConFecha (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @Column
    open var precio : Double,

    @Column
    open var desde: LocalDateTime,

    @Column
    open var hasta: LocalDateTime,

    @ManyToOne
    @PrimaryKeyJoinColumn
    open var empresa: Empresa){

/*    companion object {
        fun getPrecioByFecha(listaPrecioConFecha: MutableSet<Any>, fecha: LocalDateTime): Long {
            if(listaPrecioConFecha.isNotEmpty() && listaPrecioConFecha.any { it.desde == fecha || it.desde.isBefore(fecha) && it.hasta.isAfter(fecha) } ) {
                return listaPrecioConFecha.find { it.desde == fecha || it.desde.isBefore(fecha) && it.hasta.isAfter(fecha) }!!.precio
            }else{
                return 0
            }
        }
    }*/

}

@Entity(name = "precio_con_fecha_extra")
class PrecioConFechaExtra(
    id: Long,
    precio: Double,
    desde: LocalDateTime,
    hasta: LocalDateTime,
    empresa: Empresa,

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    val extra: Extra) : PrecioConFecha(id, precio, desde, hasta, empresa) {}

@Entity(name = "precio_con_fecha_tipo_evento")
class PrecioConFechaTipoEvento(
    id: Long,
    precio: Double,
    desde: LocalDateTime,
    hasta: LocalDateTime,
    empresa: Empresa,

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    val tipoEvento: TipoEvento): PrecioConFecha(id, precio, desde, hasta, empresa) {}

