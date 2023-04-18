package com.estonianport.agendaza.model

import com.estonianport.agendaza.dto.TimeDTO
import com.estonianport.agendaza.dto.TipoEventoDTO
import com.estonianport.agendaza.dto.TipoEventoPrecioDTO
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.CascadeType
import java.time.LocalTime
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrimaryKeyJoinColumn
import java.time.LocalDate
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
    @ManyToMany(mappedBy = "listaTipoEvento", fetch = FetchType.LAZY)
    val listaServicio: MutableSet<Servicio> = mutableSetOf()

    @JsonIgnore
    @ManyToMany(mappedBy = "listaTipoEvento", fetch = FetchType.LAZY)
    val listaExtra: MutableSet<Extra> = mutableSetOf()

    @Column
    var fechaBaja : LocalDate? = null

    fun toDTO() : TipoEventoDTO {
        return TipoEventoDTO(id, nombre, LocalTime.of(cantidadDuracion.hour, cantidadDuracion.minute),
            duracion, capacidad, empresa.id)
    }

    fun toTipoEventoPrecioDTO(fecha : LocalDateTime): TipoEventoPrecioDTO {
        return TipoEventoPrecioDTO(id, nombre, this.empresa.getPrecioOfTipoEvento(this, fecha))
    }
}