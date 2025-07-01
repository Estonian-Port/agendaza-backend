package com.estonianport.agendaza.model

import com.estonianport.agendaza.dto.PagoDTO
import com.estonianport.agendaza.model.enums.Concepto
import com.estonianport.agendaza.model.enums.MedioDePago
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrimaryKeyJoinColumn
import org.hibernate.annotations.Proxy
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Proxy(lazy = false)
open class Pago (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column
    val monto : Double,

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
    @Enumerated(EnumType.STRING)
    val concepto : Concepto){

    @Column
    var fechaBaja : LocalDate? = null

    fun toDTO() : PagoDTO{
        return PagoDTO(
            id = id,
            monto = monto,
            codigo = evento.codigo,
            medioDePago = medioDePago,
            fecha = fecha,
            nombreEvento = evento.nombre,
            concepto = concepto,
            empresaId = evento.empresa.id,
            usuarioId = encargado.id
        )
    }
}