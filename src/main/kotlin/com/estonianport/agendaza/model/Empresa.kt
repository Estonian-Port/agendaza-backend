package com.estonianport.agendaza.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@JsonTypeInfo(
    use= JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "tipo")
@JsonSubTypes(
    JsonSubTypes.Type(value = Salon::class, name = "SALON"),
    JsonSubTypes.Type(value = Catering::class, name ="CATERING"),
    JsonSubTypes.Type(value = Prestador::class, name ="PRESTADOR"))
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
abstract class Empresa(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long,

    @Column
    open val nombre: String,

    @Column
    open val telefono : Long,

    @Column
    open val email : String,

    @Column
    open val calle: String,

    @Column
    open val numero: Int,

    @Column
    open val municipio: String){

    @JsonIgnore
    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    open val listaEvento : MutableSet<Evento> = mutableSetOf()

    @JsonIgnore
    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    open val listaEmpleados: MutableSet<Cargo> = mutableSetOf()

    @JsonIgnore
    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    open val listaExtra: MutableSet<Extra> = mutableSetOf()

    @JsonIgnore
    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    open val listaPrecioConFechaExtra: MutableSet<PrecioConFechaExtra> = mutableSetOf()

    @JsonIgnore
    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    open val listaTipoEvento: MutableSet<TipoEvento> = mutableSetOf()

    @JsonIgnore
    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    open val listaPrecioConFechaTipoEvento: MutableSet<PrecioConFechaTipoEvento> = mutableSetOf()

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "empresa_servicio",
            joinColumns = arrayOf(JoinColumn(name = "empresa_id")),
            inverseJoinColumns = arrayOf(JoinColumn(name = "servicio_id"))
    )
    open var listaServicio: MutableSet<Servicio> = mutableSetOf()

    @Column
    open var fechaBaja : LocalDate? = null

    abstract fun getContacto() : List<String>

    fun getSumOfPrecioByListaExtra(listaExtra: List<Extra>, fecha : LocalDateTime): Double{
        return listaExtra.sumOf {
            getPrecioOfExtraByFecha(it, fecha)
        }
    }

    fun getSumOfPrecioByListaExtraVariable(listaExtraVariable: List<EventoExtraVariable>, fecha : LocalDateTime): Double{
        return listaExtraVariable.sumOf {
            getPrecioOfExtraVariableByFecha(it, fecha)
        }
    }

    fun getPrecioOfExtraVariableByFecha(extraVariable: EventoExtraVariable, fecha: LocalDateTime): Double{
        return getPrecioOfExtraByFecha(extraVariable.extra, fecha) * extraVariable.cantidad
    }

    fun getPrecioOfExtraByFecha(extra: Extra, fecha: LocalDateTime): Double{
        return listaPrecioConFechaExtra.find {
            it.extra.id == extra.id &&
            it.fechaBaja == null &&
            (it.desde == fecha || it.desde.isBefore(fecha) && it.hasta.isAfter(fecha))
        }?.precio ?: return 0.0

    }

    fun getPrecioOfTipoEvento(tipoEvento: TipoEvento, fecha: LocalDateTime): Double{
        return listaPrecioConFechaTipoEvento.find {
            it.tipoEvento.id == tipoEvento.id &&
            it.fechaBaja == null &&
            it.desde == fecha || it.desde.isBefore(fecha) && it.hasta.isAfter(fecha)
        }?.precio ?: return 0.0
    }

}

@Entity
open class Salon(
    id : Long,
    nombre : String,
    telefono : Long,
    email : String,
    calle : String,
    numero: Int,
    municipio: String) : Empresa(id, nombre, telefono, email, calle, numero, municipio){

    @Transient
    override fun getContacto(): List<String> {
        return arrayListOf(calle, numero.toString(), municipio)
    }
}

@Entity
open class Catering(
    id : Long,
    nombre : String,
    telefono : Long,
    email : String,
    calle : String,
    numero: Int,
    municipio: String) : Empresa(id, nombre, telefono, email, calle, numero, municipio){

    @Transient
    override fun getContacto(): List<String> {
        return listOf(telefono.toString(), email)
    }
}

@Entity
open class Prestador(
    id : Long,
    nombre : String,
    telefono : Long,
    email : String,
    calle : String,
    numero: Int,
    municipio: String,

    @Column
    @Enumerated(EnumType.STRING)
    open var tipoPrestador : TipoPrestador) : Empresa(id, nombre, telefono, email, calle, numero, municipio){

    @Transient
    override fun getContacto(): List<String> {
        return listOf(telefono.toString(), email)
    }
}