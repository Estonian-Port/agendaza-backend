package com.estonianport.agendaza.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrimaryKeyJoinColumn
import org.hibernate.annotations.Proxy
import java.time.LocalDateTime

@Entity
@Proxy(lazy = false)
open class EventoExtraVariable(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @ManyToOne
    @PrimaryKeyJoinColumn
    val extra: Extra,

    @Column
    val cantidad : Int) {

    @ManyToOne
    @PrimaryKeyJoinColumn
    lateinit var evento: Evento

}
