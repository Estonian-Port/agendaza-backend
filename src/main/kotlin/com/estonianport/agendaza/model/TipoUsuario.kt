package com.estonianport.agendaza.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.MappedSuperclass

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
abstract class TipoUsuario(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long){}

@Entity
data class Owner(

    @Column
    private val nombre: String) : TipoUsuario(0){}

@Entity
data class Encargado(

    @Column
    private val nombre: String) : TipoUsuario(0){}

@Entity
data class Empleado(

    @Column
    private val nombre: String) : TipoUsuario(0){}


@Entity
data class Caterings(

    @Column
    private val nombre: String) : TipoUsuario(0)

@Entity
data class Clientes(

    @Column
    private val nombre: String) : TipoUsuario(0)