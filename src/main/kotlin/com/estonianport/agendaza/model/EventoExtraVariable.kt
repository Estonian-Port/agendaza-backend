package com.estonianport.agendaza.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrimaryKeyJoinColumn
import java.time.LocalDateTime

@Entity
data class EventoExtraVariable(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    val extra: Extra,

    @Column
    val cantidad : Int) {

    @ManyToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    lateinit var evento: Evento

    companion object {
        fun getPrecioByFechaOfListaExtraVariable(listaExtra: List<EventoExtraVariable>, fecha: LocalDateTime): Double {
            return listaExtra.sumOf { it.extra.getPrecioByFecha(fecha) * it.cantidad }
        }
    }

}
