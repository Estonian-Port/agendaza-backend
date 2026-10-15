package com.estonianport.agendaza.model

import com.estonianport.agendaza.model.enums.Estado
import com.estonianport.agendaza.model.enums.TipoExtra
import jakarta.persistence.CascadeType
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
import jakarta.persistence.OneToMany
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
class Evento(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @Column
    var codigo: String,

    @Column
    var nombre: String,

    @Column
    var inicio: LocalDateTime,

    @Column
    var fin: LocalDateTime,

    @Column(name = "capacidad_adultos")
    var capacidadAdultos: Int,

    @Column(name = "capacidad_ninos")
    var capacidadNinos: Int,

    @ManyToOne
    @JoinColumn(name = "tipo_evento_id")
    var tipoEvento: TipoEvento,

    @Column(name = "precio_tipo_evento")
    var precioTipoEvento: Double = 0.0,

    @Column
    var extraOtro: Double,

    @Column
    var extraOtroDescripcion : String,

    @Column
    var cateringOtro : Double,

    @Column
    var cateringOtroDescripcion : String,

    @OneToMany(mappedBy = "evento", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var listaEventoExtra: MutableSet<EventoExtra>,

    @ManyToOne
    @JoinColumn(name = "encargado_id")
    var encargado: Usuario,

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    var cliente: Usuario,

    @Column
    @Enumerated(EnumType.STRING)
    var estado: Estado,

    @Column
    var anotaciones: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    var empresa : Empresa){

    @OneToMany(mappedBy = "evento", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var listaPago: MutableSet<Pago> = mutableSetOf()

    @Column
    var fechaBaja : LocalDate? = null

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "evento_empleado",
        joinColumns = [JoinColumn(name = "evento_id")],
        inverseJoinColumns = [JoinColumn(name = "usuario_id")]
    )
    var listaEmpleado: MutableSet<Usuario> = mutableSetOf()

    fun getPresupuesto(): Double{
        val presupuesto =
                precioTipoEvento +
                listaEventoExtra.filter { it.extra.tipoExtra == TipoExtra.EVENTO || it.extra.tipoExtra == TipoExtra.VARIABLE_EVENTO }
                    .sumOf { it.subtotal } +
                extraOtro
        return presupuesto
    }

    fun getPresupuestoCatering(): Double{
        return capacidadAdultos * listaEventoExtra.filter { it.extra.tipoExtra == TipoExtra.TIPO_CATERING }.sumOf { it.subtotal } +
                listaEventoExtra.filter { it.extra.tipoExtra == TipoExtra.VARIABLE_CATERING }.sumOf { it.subtotal } +
            capacidadAdultos * cateringOtro
    }

    fun getPresupuestoTotal(): Double{
        return getPresupuesto() + getPresupuestoCatering()
    }

    fun getTotalAbonado(): Double {
        return listaPago.filter{ pago -> pago.fechaBaja == null }.sumOf { it.monto }
    }

    fun getMontoFaltante(): Double {
        return getPresupuestoTotal() - getTotalAbonado()
    }
}
