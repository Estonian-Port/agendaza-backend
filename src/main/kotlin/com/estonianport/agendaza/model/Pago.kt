package com.estonianport.agendaza.model

import com.estonianport.agendaza.dto.PagoDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrimaryKeyJoinColumn
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
data class Pago (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column
    val monto : Int,

    @PrimaryKeyJoinColumn
    @Enumerated(EnumType.STRING)
    val medioDePago: MedioDePago,

    @Column
    val fecha: LocalDateTime,

    @ManyToOne
    @PrimaryKeyJoinColumn
    val evento: Evento,

    @ManyToOne
    @PrimaryKeyJoinColumn
    val encargado: Usuario,

    @Column
    var fechaBaja : LocalDate? = null){

    fun toDTO() : PagoDto{
        return PagoDto(
            id = id,
            monto = monto,
            codigo = evento.codigo,
            medioDePago = medioDePago,
            fecha = fecha,
            nombreEvento = evento.nombre
        )
    }
}