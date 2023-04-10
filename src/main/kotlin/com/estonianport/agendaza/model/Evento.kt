package com.estonianport.agendaza.model

import com.estonianport.agendaza.dto.EventoCateringDto
import com.estonianport.agendaza.dto.EventoDto
import com.estonianport.agendaza.dto.EventoExtraDto
import com.estonianport.agendaza.dto.EventoExtraVariableDto
import com.estonianport.agendaza.dto.EventoHoraDto
import com.estonianport.agendaza.dto.EventoPagoDto
import com.estonianport.agendaza.dto.EventoVerDto
import com.estonianport.agendaza.dto.ExtraDto
import com.estonianport.agendaza.dto.PagoDto
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
    var cateringOtro : Double,

    @Column
    var cateringOtroDescripcion : String,

    @ManyToOne
    @PrimaryKeyJoinColumn
    val encargado: Usuario,

    @ManyToOne(cascade = arrayOf(CascadeType.PERSIST))
    @PrimaryKeyJoinColumn
    val cliente: Usuario,

    @Column
    val codigo: String,

    @Column
    @Enumerated(EnumType.STRING)
    val estado: Estado,

    @Column
    val anotaciones: String){

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

    //TODO revisar los filter esos
    fun getPresupuesto(): Double{
        var presupuesto = tipoEvento.getPrecioByFecha(inicio) +
                Extra.getPrecioByFechaOfListaExtra(
                    listaExtra.filter { it.tipoExtra == TipoExtra.EVENTO }, inicio) +
                EventoExtraVariable.getPrecioByFechaOfListaExtraVariable(
                    listaEventoExtraVariable.filter { it.extra.tipoExtra == TipoExtra.VARIABLE_EVENTO }, inicio) +
                extraOtro

        if(descuento != 0L){
            presupuesto -= (presupuesto * (descuento / 100))
        }
        return presupuesto
    }

    fun getPresupuestoCatering(): Double{
        return capacidad.capacidadAdultos *
            Extra.getPrecioByFechaOfListaExtra(
                listaExtra.filter { it.tipoExtra == TipoExtra.TIPO_CATERING }, inicio) +
                EventoExtraVariable.getPrecioByFechaOfListaExtraVariable(
                listaEventoExtraVariable.filter { it.extra.tipoExtra == TipoExtra.VARIABLE_CATERING }, inicio) +
            capacidad.capacidadAdultos * cateringOtro
    }

    fun getPresupuestoTotal(): Double{
        return this.getPresupuesto() + this.getPresupuestoCatering()
    }

    fun toDto() : EventoDto{
        return EventoDto(id, nombre, codigo, inicio, fin, tipoEvento.nombre)
    }

    fun toEventoVerDto(listaExtraEvento : List<ExtraDto>,
                       listaExtraVariableEvento : List<EventoExtraVariableDto>,
                       listaExtraCatering : List<ExtraDto>,
                       listaExtraVariableCatering : List<EventoExtraVariableDto>) : EventoVerDto{
        return EventoVerDto(id, nombre, codigo, inicio, fin, tipoEvento.nombre, capacidad, extraOtro,
            descuento, listaExtraEvento, listaExtraVariableEvento, cateringOtro, cateringOtroDescripcion,
            listaExtraCatering, listaExtraVariableCatering, cliente, this.getPresupuestoTotal(), estado, anotaciones)
    }

    fun toEventoHoraDto(): EventoHoraDto {
     return EventoHoraDto(id, nombre, codigo, inicio, fin)
    }

    fun toEventoCateringDto(listaExtra: List<ExtraDto>,
                            listaExtraVariable: List<EventoExtraVariableDto>): EventoCateringDto {
    return EventoCateringDto(id, nombre, codigo, cateringOtro, cateringOtroDescripcion, listaExtra,
        listaExtraVariable, tipoEvento.id, inicio, capacidad)
    }

    fun toEventoExtraDto(listaExtra: List<ExtraDto>,
                         listaExtraVariable: List<EventoExtraVariableDto>): EventoExtraDto {
        return EventoExtraDto(id, nombre, codigo, extraOtro, descuento, listaExtra,
            listaExtraVariable, tipoEvento.toTipoEventoExtraDto(inicio), inicio)
    }

    fun toEventoPagoDto(listaPago: List<PagoDto>): EventoPagoDto {
        return EventoPagoDto(id, nombre, codigo, getPresupuestoTotal(), listaPago)
    }
}