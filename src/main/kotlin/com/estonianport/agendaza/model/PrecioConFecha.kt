package com.estonianport.agendaza.model

import com.estonianport.agendaza.dto.PrecioConFechaDTO
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PrimaryKeyJoinColumn
import org.hibernate.annotations.Proxy
import java.time.LocalDate
import java.time.LocalDateTime

@MappedSuperclass
@Proxy(lazy = false)
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

    @Column
    var fechaBaja : LocalDate? = null

}

@Entity(name = "precio_con_fecha_extra")
@Proxy(lazy = false)
open class PrecioConFechaExtra(
    id: Long,
    precio: Double,
    desde: LocalDateTime,
    hasta: LocalDateTime,
    empresa: Empresa,

    @JsonIgnore
    @ManyToOne
    @PrimaryKeyJoinColumn
    val extra: Extra) : PrecioConFecha(id, precio, desde, hasta, empresa) {

    fun toDTO(): PrecioConFechaDTO{
        return PrecioConFechaDTO(
            id,
            desde,
            hasta,
            precio,
            empresa.id,
            extra.id)
    }

}

@Entity(name = "precio_con_fecha_tipo_evento")
@Proxy(lazy = false)
open class PrecioConFechaTipoEvento(
    id: Long,
    precio: Double,
    desde: LocalDateTime,
    hasta: LocalDateTime,
    empresa: Empresa,

    @JsonIgnore
    @ManyToOne
    @PrimaryKeyJoinColumn
    val tipoEvento: TipoEvento): PrecioConFecha(id, precio, desde, hasta, empresa) {

    fun toDTO(): PrecioConFechaDTO{
        return PrecioConFechaDTO(
            id,
            desde,
            hasta,
            precio,
            empresa.id,
            tipoEvento.id)
    }
}

