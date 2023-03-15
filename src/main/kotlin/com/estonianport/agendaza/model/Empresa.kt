package com.estonianport.agendaza.model

import com.estonianport.agendaza.errors.BusinessException
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany

@JsonTypeInfo(
    use= JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "tipo")
@JsonSubTypes(
    JsonSubTypes.Type(value = Salon::class, name = "SALON"),
    JsonSubTypes.Type(value = Catering::class, name ="CATERING"),
    JsonSubTypes.Type(value = Prestador::class, name ="PRESTADOR"))
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
abstract class Empresa(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    open val id: Long,

    @Column
    open val nombre: String){

    @JsonIgnore
    @ManyToMany(mappedBy = "listaEmpresa", fetch = FetchType.LAZY)
    open val listaEvento : MutableSet<Evento> = mutableSetOf()

    @JsonIgnore
    @OneToMany(mappedBy = "empresa")
    open val listaEmpleados: MutableSet<Cargo> = mutableSetOf()

    @JsonIgnore
    @OneToMany(mappedBy = "empresa")
    open val listaServicio: MutableSet<Servicio> = mutableSetOf()

    @JsonIgnore
    @OneToMany(mappedBy = "empresa")
    open val listaExtra: MutableSet<Extra> = mutableSetOf()

    @JsonIgnore
    @OneToMany(mappedBy = "empresa")
    open val listaTipoEvento: MutableSet<TipoEvento> = mutableSetOf()

    fun getCargoOfUsuario(usuario: Usuario) : TipoCargo {
        return listaEmpleados.find { cargo -> cargo.usuario == usuario }
            ?.tipoCargo ?: throw BusinessException("El usuario no cuenta con un cargo en esta empresa")
    }
}

@Entity
class Salon(
    id : Long,
    nombre : String,

    @Column
    val calle: String,

    @Column
    val numero: Int,

    @Column
    val municipio: String) : Empresa(id, nombre)

@Entity
class Catering(
    id : Long,
    nombre : String,

    @Column
    val listaMenu : String) : Empresa(id, nombre)

@Entity
class Prestador(
    id : Long,
    nombre : String,

    @Column
    @Enumerated(EnumType.STRING)
    var tipoPrestador : TipoPrestador) : Empresa(id, nombre)