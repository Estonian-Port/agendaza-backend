package com.estonianport.agendaza.model

import com.estonianport.agendaza.dto.PagoDto
import com.estonianport.agendaza.dto.TimeDto
import com.estonianport.agendaza.dto.TipoEventoDto
import com.estonianport.agendaza.dto.TipoEventoExtraDto
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.CascadeType
import java.time.LocalTime
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.PrimaryKeyJoinColumn
import java.time.LocalDateTime

@Entity
data class TipoEvento(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column
    val nombre: String,

    @JoinColumn(name = "duracion")
    @Enumerated(EnumType.STRING)
    val duracion : Duracion,

    @ManyToOne(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @PrimaryKeyJoinColumn
    var capacidad: Capacidad,

    @Column
    val cantidadDuracion: LocalTime,

    @ManyToOne(cascade = [CascadeType.PERSIST])
    @PrimaryKeyJoinColumn
    val empresa: Empresa){

    @JsonIgnore
    @OneToMany(mappedBy = "tipoEvento")
    val listaPrecioConFecha: MutableSet<PrecioConFechaTipoEvento> = mutableSetOf()

    @JsonIgnore
    @ManyToMany(mappedBy = "listaTipoEvento")
    val listaServicio: MutableSet<Servicio> = mutableSetOf()

    @JsonIgnore
    @ManyToMany(mappedBy = "listaTipoEvento")
    val listaExtra: MutableSet<Extra> = mutableSetOf()

    fun toDTO() : TipoEventoDto {
        return TipoEventoDto(id, nombre, TimeDto(cantidadDuracion.hour, cantidadDuracion.minute),
            duracion, capacidad, empresa.id)
    }

    fun getPrecioByFecha(fecha : LocalDateTime): Double {
        if(listaPrecioConFecha.isNotEmpty() && listaPrecioConFecha.any { it.desde == fecha || it.desde.isBefore(fecha) && it.hasta.isAfter(fecha) } ) {
            return listaPrecioConFecha.find { it.desde == fecha || it.desde.isBefore(fecha) && it.hasta.isAfter(fecha) }!!.precio
        }else{
            return 0.0
        }
    }

    fun toTipoEventoExtraDto(fecha : LocalDateTime): TipoEventoExtraDto {
        return TipoEventoExtraDto(id, nombre, this.getPrecioByFecha(fecha))
    }
}