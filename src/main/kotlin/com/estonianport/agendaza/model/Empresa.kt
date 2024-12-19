package com.estonianport.agendaza.model

import com.estonianport.agendaza.dto.GenericItemDTO
import com.estonianport.agendaza.errors.BusinessException
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import jakarta.persistence.*
import org.hibernate.annotations.Proxy
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
@Proxy(lazy = false)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
abstract class Empresa(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column
    val nombre: String,

    @Column
    val telefono : Long,

    @Column
    val email : String,

    @Column
    val calle: String,

    @Column
    val numero: Int,

    @Column
    val municipio: String) {


    @JsonIgnore
    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    val listaEvento : MutableSet<Evento> = mutableSetOf()

    @JsonIgnore
    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    val listaEmpleados: MutableSet<Cargo> = mutableSetOf()

    @JsonIgnore
    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    val listaExtra: MutableSet<Extra> = mutableSetOf()

    @JsonIgnore
    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    val listaPrecioConFechaExtra: MutableSet<PrecioConFechaExtra> = mutableSetOf()

    @JsonIgnore
    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    val listaTipoEvento: MutableSet<TipoEvento> = mutableSetOf()

    @JsonIgnore
    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    val listaPrecioConFechaTipoEvento: MutableSet<PrecioConFechaTipoEvento> = mutableSetOf()

    @JsonIgnore
    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    val listaEspecificacion: MutableList<Especificacion> = mutableListOf()

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "empresa_servicio",
            joinColumns = arrayOf(JoinColumn(name = "empresa_id")),
            inverseJoinColumns = arrayOf(JoinColumn(name = "servicio_id"))
    )
    var listaServicio: MutableSet<Servicio> = mutableSetOf()

    @Column
    var fechaBaja : LocalDate? = null


    fun recorrerEspecificaciones(evento: Evento) {
        listaEspecificacion.forEach { especificacion ->
            especificacion.aplicar(evento)
        }
    }

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

    fun copy(nombre: String, telefono: Long, email: String, calle: String, numero: Int, municipio: String
    ): Empresa {
        return when (this) {
            is Salon -> Salon(id, nombre, telefono, email, calle, numero, municipio)
            is Catering -> Catering(id, nombre, telefono, email, calle, numero, municipio)
            is Prestador -> Prestador(id, nombre, telefono, email, calle, numero, municipio, tipoPrestador)
            else -> throw BusinessException("Tipo de empresa no soportado")
        }
    }

    fun toGenericItemDTO(): GenericItemDTO {
        return GenericItemDTO(id, nombre)
    }

}

@Entity
@Proxy(lazy = false)
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
@Proxy(lazy = false)
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
@Proxy(lazy = false)
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