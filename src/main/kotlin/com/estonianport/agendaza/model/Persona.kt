package com.estonianport.agendaza.model

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
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
@DiscriminatorColumn(name="tipo")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
abstract class Persona(

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
    val mail: String){}

@Entity
@DiscriminatorValue(value="USUARIO")
class Usuario(
    id: Long,
    nombre: String,
    apellido: String,
    cuil: Long,
    celular: Long,
    mail: String,

    @Column
    val username: String,

    @Column
    val password: String,

    @Column
    val tipoUsuario : TipoUsuario) : Persona(id,nombre, apellido, cuil, celular, mail){}

@Entity
@DiscriminatorValue(value="CLIENTE")
class Cliente(
    id: Long,
    nombre: String,
    apellido: String,
    cuil: Long,
    celular: Long,
    mail: String,

    @PrimaryKeyJoinColumn
    val sexo: Sexo,

    @Column(name = "fecha_nacimiento")
    val fechaNacimiento: Date,

    @Column
    val empresa: String,

    @Column
    val ciudad: String,

    @Column
    val provincia: String,

    @Column
    val codigoPostal : Int,

    @JsonBackReference
    @OneToMany(mappedBy = "cliente")
    val evento: Set<Evento>) : Persona(id,nombre, apellido, cuil, celular, mail){}