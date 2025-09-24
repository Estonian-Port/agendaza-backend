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
 class Pago(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @Column
    var monto: Double,

    @Column
    @Enumerated(EnumType.STRING)
    var concepto: Concepto,

    @Column
    @Enumerated(EnumType.STRING)
    var medioDePago: MedioDePago,

    @Column
    var fecha: LocalDateTime,

    @ManyToOne
    @JoinColumn(name = "evento_id")
    var evento: Evento,

    @ManyToOne
    @JoinColumn(name = "encargado_id")
    var encargado: Usuario,

    @Column
    var numeroCuota: String? = null,

    @Column
    var fechaBaja: LocalDate? = null
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