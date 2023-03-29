package com.estonianport.agendaza.model

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
import jakarta.persistence.PrimaryKeyJoinColumn
import java.time.LocalDateTime

@Entity
data class Evento(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column
    val nombre: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    val tipoEvento: TipoEvento,

    @Column
    var inicio: LocalDateTime,

    @Column
    var fin: LocalDateTime,

    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @PrimaryKeyJoinColumn
    var capacidad: Capacidad,

    @Column
    var extraOtro: Long,

    @Column
    var descuento : Long,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "evento_extra",
        joinColumns = arrayOf(JoinColumn(name = "agregados_id")),
        inverseJoinColumns = arrayOf(JoinColumn(name = "extra_id"))
    )
    var listaExtra: MutableSet<Extra>,

    @OneToMany(mappedBy = "evento")
    var listaEventoExtraVariable: MutableSet<EventoExtraVariable>,

    @Column
    var cateringOtro : Long,

    @Column
    var cateringOtroDescripcion : String,

    @ManyToOne
    @PrimaryKeyJoinColumn
    val encargado: Usuario,

    @ManyToOne
    @PrimaryKeyJoinColumn
    val cliente: Usuario,

    @Column
    val codigo: String,

    @Column
    @Enumerated(EnumType.STRING)
    val estado: Estado){

    @ManyToMany
    @JoinTable(
        name = "evento_empresa",
        joinColumns = arrayOf(JoinColumn(name = "evento_id")),
        inverseJoinColumns = arrayOf(JoinColumn(name = "empresa_id"))
    )
    val listaEmpresa: MutableSet<Empresa> = mutableSetOf()

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "evento_empleado",
        joinColumns = arrayOf(JoinColumn(name = "evento_id")),
        inverseJoinColumns = arrayOf(JoinColumn(name = "usuario_id"))
    )
    val listaEmpleado: MutableSet<Usuario> = mutableSetOf()

    @OneToMany(mappedBy = "evento", cascade = arrayOf(CascadeType.ALL))
    val listaPago: MutableSet<Pago> = mutableSetOf()

    fun getPresupuesto(): Double{
        var presupuesto = tipoEvento.getPrecioByFecha(inicio) +
                Extra.getPrecioByFechaOfListaExtra(
                    listaExtra.filter { it.tipoExtra == TipoExtra.EVENTO }, inicio) +
                Extra.getPrecioByFechaOfListaExtra(
                    listaEventoExtraVariable.filter { it.extra.tipoExtra == TipoExtra.VARIABLE_EVENTO }.
                    map { it.extra }, inicio) +
                extraOtro

        if(descuento != 0L){
            presupuesto -= (presupuesto * (descuento / 100))
        }
        return presupuesto
    }

    fun getPresupuestoCatering(): Double{
        return 0.0
    }

    fun getPresupuestoTotal(): Double{
        return this.getPresupuesto() + this.getPresupuestoCatering()
    }

}