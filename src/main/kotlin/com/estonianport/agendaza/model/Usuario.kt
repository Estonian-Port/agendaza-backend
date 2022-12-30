package com.estonianport.agendaza.model

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
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
import java.sql.Date

@Entity
data class Usuario(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column
    val nombre: String,

    @Column
    val apellido: String,

    @Column
    val cuil: Long,

    @Column
    val celular: Long,

    @Column
    val mail: String,

    @PrimaryKeyJoinColumn
    val sexo: Sexo,

    @Column
    val username: String,

    @Column
    val password: String,

    @Column(name = "fecha_nacimiento")
    val fechaNacimiento: Date,

    @Embedded
    val tipoUsuario : TipoUsuario) {

    @ManyToMany
    @JoinTable(
        name = "empresa_empleado",
        joinColumns = arrayOf(JoinColumn(name = "usuario_id")),
        inverseJoinColumns = arrayOf(JoinColumn(name = "empresa_id"))
    )
    val listaEmpresa: MutableSet<Empresa> = mutableSetOf()

}



