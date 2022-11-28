package com.estonianport.agendaza.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.MappedSuperclass
/*
@MappedSuperclass
abstract class TipoUsuario(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column
    val nombre2: String){}


@Entity
data class Owner(

    @Column
    val nombre: String) : TipoUsuario(0, ""){}

@Entity
data class Encargado(

    @Column
    val nombre: String) : TipoUsuario(0, ""){}

@Entity
data class Empleado(

    @Column
    val nombre: String) : TipoUsuario(0, ""){}


@Entity
data class Caterings(

    @Column
    val nombre: String) : TipoUsuario(0, "")

@Entity
data class Clientes(

    @Column
    val nombre: String) : TipoUsuario(0, "")*/