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
    var nombre: String,

    @ManyToOne
    @JoinColumn(name = "tipo_evento_id")
    var tipoEvento: TipoEvento,

    @Column
    var inicio: LocalDateTime,

    @Column
    var fin: LocalDateTime,

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "capacidad_id")
    var capacidad: Capacidad,

    @Column
    var extraOtro: Double,

    @Column
    var descuento : Long,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "evento_extra",
        joinColumns = [JoinColumn(name = "evento_id")],
        inverseJoinColumns = [JoinColumn(name = "extra_id")]
    )
    var listaExtra: MutableSet<Extra>,

    @OneToMany(mappedBy = "evento", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var listaEventoExtraVariable: MutableSet<EventoExtraVariable> = mutableSetOf(),

    @Column
    var cateringOtro : Double,

    @Column
    var cateringOtroDescripcion : String,

    @ManyToOne
    @JoinColumn(name = "encargado_id")
    var encargado: Usuario,

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    var cliente: Usuario,

    @Column
    var codigo: String,

    @Column
    @Enumerated(EnumType.STRING)
    var estado: Estado,

    @Column
    var anotaciones: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    var empresa : Empresa){

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "evento_empleado",
        joinColumns = [JoinColumn(name = "evento_id")],
        inverseJoinColumns = [JoinColumn(name = "usuario_id")]
    )
    var listaEmpleado: MutableSet<Usuario> = mutableSetOf()

    @OneToMany(mappedBy = "evento", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var listaPago: MutableSet<Pago> = mutableSetOf()

    @Column
    var fechaBaja : LocalDate? = null

    //TODO Simplificar los filter
    fun getPresupuesto(): Double{
        var presupuesto =
                empresa.getPrecioOfTipoEvento(tipoEvento.id, inicio) +
                empresa.getSumOfPrecioByListaExtra(
                    listaExtra.filter { it.tipoExtra == TipoExtra.EVENTO }, inicio) +
                empresa.getSumOfPrecioByListaExtraVariable(
                    listaEventoExtraVariable.filter { it.extra.tipoExtra == TipoExtra.VARIABLE_EVENTO }, inicio) +
                extraOtro

        if(descuento != 0L){
            presupuesto -= (presupuesto * (descuento / 100.0))
        }
        return presupuesto
    }

    fun getPresupuestoCatering(): Double{
        return capacidad.capacidadAdultos *
                empresa.getSumOfPrecioByListaExtra(
                    listaExtra.filter { it.tipoExtra == TipoExtra.TIPO_CATERING }, inicio) +
                empresa.getSumOfPrecioByListaExtraVariable(
                    listaEventoExtraVariable.filter { it.extra.tipoExtra == TipoExtra.VARIABLE_CATERING }, inicio) +
            capacidad.capacidadAdultos * cateringOtro
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