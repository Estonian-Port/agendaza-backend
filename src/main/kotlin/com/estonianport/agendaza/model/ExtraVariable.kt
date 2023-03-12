package com.estonianport.agendaza.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrimaryKeyJoinColumn

@Entity
class EventoExtraVariableTipoEvento(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    val extra: Extra,

    @Column
    val cantidad : Int){

    @ManyToOne
    @PrimaryKeyJoinColumn
    var agregados: Agregados = Agregados(0,0,0, mutableSetOf(), mutableSetOf())
}

@Entity
class CateringEventoExtraVariableCatering(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    val extra: Extra,

    @Column
    val cantidad : Int){

    @ManyToOne
    @PrimaryKeyJoinColumn
    var cateringEvento: CateringEvento = CateringEvento(0,0,0,"", mutableSetOf(), mutableSetOf())
}

/*

@JsonTypeInfo(
    use= JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "tipo")
@JsonSubTypes(
    JsonSubTypes.Type(value = EventoExtraVariableTipoEvento::class, name = "EVENTO_EXTRA_VARIABLE"),
    JsonSubTypes.Type(value = CateringEventoExtraVariableCatering::class, name ="CATERING_EXTRA_VARIABLE"))
Entity
@Inheritance(strategy = InheritanceType.JOINED)
abstract class ExtraVariableA(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    open val extra: Extra,

    @Column
    open val cantidad : Int){}

Entity
class EventoExtraVariableTipoEvento(
    id : Long,
    extra : Extra,
    cantidad : Int) : ExtraVariable(id, extra, cantidad) {

    @ManyToOne
    @PrimaryKeyJoinColumn
    var agregados: Agregados = Agregados(0,0,0, mutableSetOf(), mutableSetOf())
}

Entity
class CateringEventoExtraVariableCatering(
    id : Long,
    extra : Extra,
    cantidad : Int) : ExtraVariable(id, extra, cantidad) {


    @ManyToOne
    @PrimaryKeyJoinColumn
    var cateringEvento: CateringEvento = CateringEvento(0,0,0,"", mutableSetOf(), mutableSetOf())
}*/
