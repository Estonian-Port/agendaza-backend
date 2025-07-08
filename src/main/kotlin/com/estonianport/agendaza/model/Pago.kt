package com.estonianport.agendaza.model

import com.estonianport.agendaza.dto.PagoDTO
import com.estonianport.agendaza.model.enums.Concepto
import com.estonianport.agendaza.model.enums.MedioDePago
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorType
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "concepto", discriminatorType = DiscriminatorType.STRING)
abstract class Pago(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long,

    @Column
    open val monto: Double,

    @Column(insertable = false, updatable = false)
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
    open var fechaBaja: LocalDate? = null
) {

    abstract fun getConceptoString(): String

    open fun toDTO(): PagoDTO {
        return PagoDTO(
            id = id,
            monto = monto,
            codigo = evento.codigo,
            medioDePago = medioDePago,
            fecha = fecha,
            nombreEvento = evento.nombre,
            concepto = concepto,
            numeroCuota = if (this is Cuota) this.numeroCuota else null,
            empresaId = evento.empresa.id,
            usuarioId = encargado.id
        )
    }

    companion object{
        fun build(id : Long, monto: Double, concepto : Concepto, medioDePago: MedioDePago, fecha: LocalDateTime, evento: Evento, encargado: Usuario, numeroCuota: Int? = null): Pago {
            return when (concepto) {
                Concepto.CUOTA -> Cuota(id, monto, Concepto.CUOTA, medioDePago, fecha, evento, encargado, numeroCuota)
                Concepto.SENIA -> Senia(id, monto, Concepto.SENIA, medioDePago, fecha, evento, encargado)
                Concepto.PAGO_TOTAL -> PagoTotal(id, monto, Concepto.PAGO_TOTAL, medioDePago, fecha, evento, encargado)
                Concepto.ADELANTO -> Adelanto(id, monto, Concepto.ADELANTO, medioDePago, fecha, evento, encargado)
            }
        }
    }
}

@Entity
@DiscriminatorValue("CUOTA")
open class Cuota(
    id: Long,
    monto: Double,
    concepto: Concepto,
    medioDePago: MedioDePago,
    fecha: LocalDateTime,
    evento: Evento,
    encargado: Usuario,
    open val numeroCuota: Int?,
    fechaBaja: LocalDate? = null
) : Pago(id, monto, concepto, medioDePago, fecha, evento, encargado, fechaBaja) {

    override fun getConceptoString(): String =
        if (numeroCuota == null || numeroCuota == 0) "Cuota" else "Cuota Nº$numeroCuota"
}

@Entity
@DiscriminatorValue("SENIA")
open class Senia(
    id: Long,
    monto: Double,
    concepto: Concepto,
    medioDePago: MedioDePago,
    fecha: LocalDateTime,
    evento: Evento,
    encargado: Usuario,
    fechaBaja: LocalDate? = null
) : Pago(id, monto, concepto, medioDePago, fecha, evento, encargado, fechaBaja) {

    override fun getConceptoString(): String = "Seña"
}

@Entity
@DiscriminatorValue("PAGO_TOTAL")
open class PagoTotal(
    id: Long,
    monto: Double,
    concepto: Concepto,
    medioDePago: MedioDePago,
    fecha: LocalDateTime,
    evento: Evento,
    encargado: Usuario,
    fechaBaja: LocalDate? = null
) : Pago(id, monto, concepto, medioDePago, fecha, evento, encargado, fechaBaja) {

    override fun getConceptoString(): String = "Pago Total"
}

@Entity
@DiscriminatorValue("ADELANTO")
open class Adelanto(
    id: Long,
    monto: Double,
    concepto: Concepto,
    medioDePago: MedioDePago,
    fecha: LocalDateTime,
    evento: Evento,
    encargado: Usuario,
    fechaBaja: LocalDate? = null
) : Pago(id, monto, concepto, medioDePago, fecha, evento, encargado, fechaBaja) {

    override fun getConceptoString(): String = "Adelanto"
}
