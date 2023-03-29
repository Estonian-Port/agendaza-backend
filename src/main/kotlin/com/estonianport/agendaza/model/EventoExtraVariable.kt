package com.estonianport.agendaza.model

import com.estonianport.agendaza.dto.ExtraVariableReservaDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrimaryKeyJoinColumn

@Entity
data class EventoExtraVariable(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    val extra: Extra,

    @Column
    val cantidad : Int){

    @ManyToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    lateinit var evento: Evento



    fun fromDTO(eventoExtraVariableReservaDto: ExtraVariableReservaDto){
        // TODO
    }

    fun toDTO(){
        // TODO
    }
}
