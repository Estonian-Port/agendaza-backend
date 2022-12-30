package com.estonianport.agendaza.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany

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

    @ManyToMany(mappedBy = "listaEmpresa")
    open val listaEmpleado : MutableSet<Usuario> = mutableSetOf()
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
    var tipoDeServicio : String) : Empresa(id, nombre)