package com.estonianport.agendaza.model

import com.estonianport.agendaza.errors.BusinessException
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany
import java.lang.Error

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
open abstract class Empresa(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    open val id: Long,

    @Column
    open val nombre: String){

    @ManyToMany(mappedBy = "listaEmpresa")
    open val listaEvento : MutableSet<Evento> = mutableSetOf()

    @OneToMany(mappedBy = "empresa", cascade = arrayOf(CascadeType.ALL))
    open val listaEmpleados: MutableSet<Cargo> = mutableSetOf()

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