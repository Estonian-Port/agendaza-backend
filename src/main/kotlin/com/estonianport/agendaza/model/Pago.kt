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
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
open class Pago(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long,

    @Column
    open val monto: Double,

    @Column
    @Enumerated(EnumType.STRING)
    open val concepto: Concepto,

    @Column
    @Enumerated(EnumType.STRING)
    open val medioDePago: MedioDePago,

    @Column
    open val fecha: LocalDateTime,

    @ManyToOne
    @JoinColumn(name = "evento_id")
    open val evento: Evento,

    @ManyToOne
    @JoinColumn(name = "encargado_id")
    open val encargado: Usuario,

    @Column
    open var numeroCuota: String? = null,

    @Column
    open var fechaBaja: LocalDate? = null
) {

    fun toDTO(): PagoDTO {
        return PagoDTO(
            id = id,
            monto = monto,
            codigo = evento.codigo,
            medioDePago = medioDePago,
            fechaEvento = evento.inicio,
            nombreEvento = evento.nombre,
            concepto = concepto,
            numeroCuota = numeroCuota,
            empresaId = evento.empresa.id,
            usuarioId = encargado.id,
            fecha = fecha
        )
    }
}