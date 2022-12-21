package com.estonianport.agendaza.model

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorValue
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
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.OneToMany
import jakarta.persistence.PrimaryKeyJoinColumn
import java.sql.Date

@JsonTypeInfo(
    use= JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "tipo")
@JsonSubTypes(
    JsonSubTypes.Type(value = Usuario::class, name = "USUARIO"),
    JsonSubTypes.Type(value = Cliente::class, name ="CLIENTE"))
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
open class Persona(

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
    val sexo: Sexo){

}

@Entity
class Usuario(
    id: Long,
    nombre: String,
    apellido: String,
    cuil: Long,
    celular: Long,
    mail: String,
    sexo: Sexo,

    @Column
    val username: String,

    @Column
    val password: String,

    @Column(name = "fecha_nacimiento")
    val fechaNacimiento: Date,

    @Column
    val tipoUsuario : TipoUsuario) : Persona(id,nombre, apellido, cuil, celular, mail, sexo){

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "usuario_salon",
        joinColumns = arrayOf(JoinColumn(name = "usuario_id")),
        inverseJoinColumns = arrayOf(JoinColumn(name = "salon_id"))
    )
    lateinit var listaSalon : Set<Salon>
    }

@Entity
class Cliente(
    id: Long,
    nombre: String,
    apellido: String,
    cuil: Long,
    celular: Long,
    mail: String,
    sexo: Sexo,

    @JsonBackReference
    @OneToMany(mappedBy = "cliente")
    val listaEvento: Set<Evento>) : Persona(id,nombre, apellido, cuil, celular, mail, sexo){}