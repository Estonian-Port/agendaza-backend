package com.estonianport.agendaza.model

import com.estonianport.agendaza.dto.*
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
import org.hibernate.annotations.Proxy
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Proxy(lazy = false)
open class Evento(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column
    var nombre: String,

    @ManyToOne
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
    var extraOtro: Double,

    @Column
    var descuento : Long,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "evento_extra",
        joinColumns = arrayOf(JoinColumn(name = "evento_id")),
        inverseJoinColumns = arrayOf(JoinColumn(name = "extra_id"))
    )
    var listaExtra: MutableSet<Extra>,

    @OneToMany(mappedBy = "evento", fetch = FetchType.LAZY)
    var listaEventoExtraVariable: MutableSet<EventoExtraVariable>,

    @Column
    var cateringOtro : Double,

    @Column
    var cateringOtroDescripcion : String,

    @ManyToOne
    @PrimaryKeyJoinColumn
    val encargado: Usuario,

    @ManyToOne
    @PrimaryKeyJoinColumn
    var cliente: Usuario,

    @Column
    val codigo: String,

    @Column
    @Enumerated(EnumType.STRING)
    val estado: Estado,

    @Column
    var anotaciones: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    val empresa : Empresa){

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "evento_empleado",
        joinColumns = arrayOf(JoinColumn(name = "evento_id")),
        inverseJoinColumns = arrayOf(JoinColumn(name = "usuario_id"))
    )
    val listaEmpleado: MutableSet<Usuario> = mutableSetOf()

    @OneToMany(mappedBy = "evento", cascade = arrayOf(CascadeType.ALL), fetch = FetchType.LAZY)
    val listaPago: MutableSet<Pago> = mutableSetOf()

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
            presupuesto -= (presupuesto * (descuento / 100))
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
        return listaPago.sumOf { it.monto }
    }

    fun getMontoFaltante(): Double {
        return getPresupuestoTotal() - getTotalAbonado()
    }

    fun toDto() : EventoDTO{
        return EventoDTO(id, nombre, codigo, inicio, fin, tipoEvento.nombre)
    }

    fun toEventoVerDto(listaExtraEvento : List<ExtraDTO>,
                       listaExtraVariableEvento : List<EventoExtraVariableDTO>,
                       listaExtraCatering : List<ExtraDTO>,
                       listaExtraVariableCatering : List<EventoExtraVariableDTO>) : EventoVerDTO{
        return EventoVerDTO(id, nombre, codigo, inicio, fin, tipoEvento.nombre, capacidad, extraOtro,
            descuento, listaExtraEvento, listaExtraVariableEvento, cateringOtro, cateringOtroDescripcion,
            listaExtraCatering, listaExtraVariableCatering, encargado.toUsuarioAbmDto(), cliente, this.getPresupuestoTotal(), estado, anotaciones)
    }

    fun toEventoReservaDto(
            listaExtraEvento: List<ExtraDTO>,
            listaExtraVariableEvento: List<EventoExtraVariableDTO>,
            listaExtraCatering: List<ExtraDTO>,
            listaExtraVariableCatering: List<EventoExtraVariableDTO>
    ): EventoReservaDTO {
        return EventoReservaDTO(id, nombre, capacidad, codigo, inicio, fin, tipoEvento.id,
                empresa.id, extraOtro, descuento, listaExtraEvento, listaExtraVariableEvento,
                cateringOtro, cateringOtroDescripcion, listaExtraCatering, listaExtraVariableCatering,
                cliente, encargado.id, estado, anotaciones
        )
    }

    fun toEventoHoraDto(): EventoHoraDTO {
     return EventoHoraDTO(id, nombre, codigo, inicio, fin)
    }

    fun toEventoCateringDto(listaExtra: List<ExtraDTO>,
                            listaExtraVariable: List<EventoExtraVariableDTO>): EventoCateringDTO {
    return EventoCateringDTO(id, nombre, codigo, cateringOtro, cateringOtroDescripcion, listaExtra,
        listaExtraVariable, tipoEvento.id, inicio, capacidad)
    }

    fun toEventoExtraDto(listaExtra: List<ExtraDTO>,
                         listaExtraVariable: List<EventoExtraVariableDTO>): EventoExtraDTO {
        return EventoExtraDTO(id, nombre, codigo, extraOtro, descuento, listaExtra,
            listaExtraVariable, empresa.toTipoEventoPrecioDTO(inicio, tipoEvento), inicio)
    }

    fun toEventoPagoDto(listaPago: List<PagoDTO>): EventoPagoDTO {
        return EventoPagoDTO(id, nombre, codigo, getPresupuestoTotal(), listaPago)
    }

    fun toEventoUsuarioDto(evento: Evento): EventoUsuarioDTO {
        return EventoUsuarioDTO(evento.id, evento.nombre, evento.codigo, evento.cliente.toUsuarioAbmDto())
    }

}