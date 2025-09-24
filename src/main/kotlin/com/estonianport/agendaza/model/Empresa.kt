package com.estonianport.agendaza.model

import com.estonianport.agendaza.dto.GenericItemDTO
import com.estonianport.agendaza.dto.TipoEventoPrecioDTO
import com.estonianport.agendaza.errors.BusinessException
import com.estonianport.agendaza.model.enums.TipoPrestador
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
    var id: Long,

    @Column
    var nombre: String,

    @Column
    var telefono : Long,

    @Column
    var email : String,

    @Column
    var calle: String,

    @Column
    var numero: Int,

    @Column
    var municipio: String) {


    @JsonIgnore
    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    var listaEvento : MutableSet<Evento> = mutableSetOf()

    @JsonIgnore
    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    var listaEmpleados: MutableSet<Cargo> = mutableSetOf()

    @JsonIgnore
    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    var listaPrecioConFechaExtra: MutableSet<PrecioConFechaExtra> = mutableSetOf()

    @JsonIgnore
    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    var listaPrecioConFechaTipoEvento: MutableSet<PrecioConFechaTipoEvento> = mutableSetOf()

    @JsonIgnore
    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    var listaEspecificacion: MutableList<Especificacion> = mutableListOf()

    @JsonIgnore
    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    var listaTipoEvento: MutableSet<TipoEvento> = mutableSetOf()

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "empresa_extra",
            joinColumns = [JoinColumn(name = "empresa_id")],
            inverseJoinColumns = [JoinColumn(name = "extra_id")]
    )
    var listaExtra: MutableSet<Extra> = mutableSetOf()

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "empresa_servicio",
            joinColumns = [JoinColumn(name = "empresa_id")],
            inverseJoinColumns = [JoinColumn(name = "servicio_id")]
    )
    var listaServicio: MutableSet<Servicio> = mutableSetOf()

    @JsonIgnore
    @ManyToMany
    @JoinTable(
        name = "empresa_clausula",
        joinColumns = [JoinColumn(name = "empresa_id")],
        inverseJoinColumns = [JoinColumn(name = "clasusula_id")]
    )
    var listaClausula: MutableSet<Clausula> = mutableSetOf()

    @Column
    var fechaBaja : LocalDate? = null

    @Column
    var logo : String = "https://iili.io/3SVIGuj.png" //Agendaza Logo por defecto

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

    // TODO Pasar a consulta SQL
    fun getPrecioOfExtraByFecha(extra: Extra, fecha: LocalDateTime): Double{
        return listaPrecioConFechaExtra.find {
            it.extra.id == extra.id &&
            it.fechaBaja == null &&
            (it.desde == fecha || it.desde.isBefore(fecha) && it.hasta.isAfter(fecha))
        }?.precio ?: return 0.0

    }

    // TODO Pasar a consulta SQL
    fun getPrecioOfTipoEvento(tipoEventoId: Long, fecha: LocalDateTime): Double{
        return listaPrecioConFechaTipoEvento.find {
            it.tipoEvento.id == tipoEventoId &&
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

    fun toTipoEventoPrecioDTO(fecha : LocalDateTime, tipoEvento: TipoEvento): TipoEventoPrecioDTO {
        return TipoEventoPrecioDTO(tipoEvento.id, tipoEvento.nombre, this.getPrecioOfTipoEvento(tipoEvento.id, fecha))
    }
}

@Entity
class Salon(
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
class Catering(
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
class Prestador(
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