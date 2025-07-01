package com.estonianport.agendaza.model

import com.estonianport.agendaza.dto.PagoDTO
import com.estonianport.agendaza.errors.BusinessException
import com.estonianport.agendaza.model.enums.Concepto
import com.estonianport.agendaza.model.enums.MedioDePago
import com.estonianport.agendaza.model.enums.TipoExtra
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
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
import org.hibernate.LockMode
import org.hibernate.annotations.Proxy
import java.time.LocalDate
import java.time.LocalDateTime

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "concepto"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = PagoCuota::class, name = "CUOTA"),
    JsonSubTypes.Type(value = PagoSenia::class, name = "SENIA"),
    JsonSubTypes.Type(value = PagoTotal::class, name = "PAGO_TOTAL")
)
@Entity
@Proxy(lazy = false)
@DiscriminatorColumn(name = "concepto", discriminatorType = DiscriminatorType.STRING)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
abstract class Pago(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long,

    @Column
    val monto: Double,

    @Column(insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    val concepto : Concepto,

    @Column
    @Enumerated(EnumType.STRING)
    val medioDePago: MedioDePago,

    @Column
    val fecha: LocalDateTime,

    @ManyToOne
    @JoinColumn(name = "evento_id")
    val evento: Evento,

    @ManyToOne
    @JoinColumn(name = "encargado_id")
    val encargado: Usuario,

    @Column
    var fechaBaja: LocalDate? = null
){

    abstract fun getConceptoString() : String

    open fun toDTO(): PagoDTO {
        return PagoDTO(
            id = id,
            monto = monto,
            codigo = evento.codigo,
            medioDePago = medioDePago,
            fecha = fecha,
            nombreEvento = evento.nombre,
            concepto = Concepto.SENIA,
            numeroCuota = 0,
            empresaId = evento.empresa.id,
            usuarioId = encargado.id
        )
    }
}

@Entity
@DiscriminatorValue("CUOTA")
open class PagoCuota(
    id : Long,
    monto: Double,
    concepto: Concepto,
    medioDePago: MedioDePago,
    fecha: LocalDateTime,
    evento: Evento,
    encargado: Usuario,
    open val numeroCuota: Int,
    fechaBaja: LocalDate? = null) : Pago(id, monto, concepto, medioDePago, fecha, evento, encargado, fechaBaja){

    override fun toDTO(): PagoDTO {
        return PagoDTO(
            id = id,
            monto = monto,
            codigo = evento.codigo,
            medioDePago = medioDePago,
            fecha = fecha,
            nombreEvento = evento.nombre,
            concepto = Concepto.CUOTA,
            numeroCuota = numeroCuota,
            empresaId = evento.empresa.id,
            usuarioId = encargado.id
        )
    }

    override fun getConceptoString(): String{
        return "Cuota $numeroCuota"
    }
}

@Entity
@DiscriminatorValue("SENIA")
open class PagoSenia(
    id : Long,
    monto: Double,
    concepto: Concepto,
    medioDePago: MedioDePago,
    fecha: LocalDateTime,
    evento: Evento,
    encargado: Usuario,
    fechaBaja: LocalDate? = null) : Pago(id, monto, concepto, medioDePago, fecha, evento, encargado, fechaBaja){

    override fun toDTO(): PagoDTO {
        return PagoDTO(
            id = id,
            monto = monto,
            codigo = evento.codigo,
            medioDePago = medioDePago,
            fecha = fecha,
            nombreEvento = evento.nombre,
            concepto = Concepto.SENIA,
            numeroCuota = 0,
            empresaId = evento.empresa.id,
            usuarioId = encargado.id
        )
    }

    override fun getConceptoString(): String{
        return "Seña"
    }
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
    fechaBaja: LocalDate? = null) : Pago(id, monto, concepto,medioDePago, fecha, evento, encargado, fechaBaja){

    override fun toDTO(): PagoDTO {
        return PagoDTO(
            id = id,
            monto = monto,
            codigo = evento.codigo,
            medioDePago = medioDePago,
            fecha = fecha,
            nombreEvento = evento.nombre,
            concepto = Concepto.PAGO_TOTAL,
            numeroCuota = 0,
            empresaId = evento.empresa.id,
            usuarioId = encargado.id
        )
    }

    override fun getConceptoString(): String {
        return "Pago Total"
    }
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
    fechaBaja: LocalDate? = null) : Pago(id, monto, concepto,medioDePago, fecha, evento, encargado, fechaBaja){

    override fun toDTO(): PagoDTO {
        return PagoDTO(
            id = id,
            monto = monto,
            codigo = evento.codigo,
            medioDePago = medioDePago,
            fecha = fecha,
            nombreEvento = evento.nombre,
            concepto = Concepto.ADELANTO,
            numeroCuota = 0,
            empresaId = evento.empresa.id,
            usuarioId = encargado.id
        )
    }

    override fun getConceptoString(): String {
        return "Adelanto"
    }
}