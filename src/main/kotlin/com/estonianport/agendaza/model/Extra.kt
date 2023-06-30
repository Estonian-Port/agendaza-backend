package com.estonianport.agendaza.model

import com.estonianport.agendaza.dto.ExtraDTO
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrimaryKeyJoinColumn
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
data class Extra(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column
    val nombre: String,

    @Column
    @Enumerated(EnumType.STRING)
    val tipoExtra : TipoExtra,

    @ManyToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    val empresa: Empresa){

    @Column
    var fechaBaja : LocalDate? = null

    fun toDTO(): ExtraDTO {
        return ExtraDTO(id, nombre, tipoExtra, empresa.id)
    }

    fun toExtraPrecioDTO(fechaEvento: LocalDateTime): ExtraDTO {
        val extraDTO = ExtraDTO(id, nombre, tipoExtra, empresa.id)
        extraDTO.precio = empresa.getPrecioOfExtraByFecha(this, fechaEvento)
        return extraDTO
    }
}