package com.estonianport.agendaza.model

import com.fasterxml.jackson.annotation.JsonBackReference
import java.time.LocalTime
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany

@Entity
data class SubTipoEvento(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long,

    @Column
    private val nombre: String,

    @JoinColumn(name = "tipo_evento")
    private val tipoEvento: TipoEvento,

    @ManyToOne
    @JoinColumn(name = "capacidad_id")
    private val capacidad: Capacidad,

    @Column
    private val duracion: LocalTime,

    @Column(name = "cant_personal")
    private val cantPersonal : Int,

    @Column(name = "valor_fin_semana")
    private val valorFinSemana : Int,

    @Column(name = "horario_final_automatico")
    private val horarioFinalAutomatico : Boolean,

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "subTipoEvento")
    private val listaPrecioConFecha: List<PrecioConFechaSubTipoEvento>,

    @JsonBackReference
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "listaSubTipoEvento")
    private val listaServicio: Set<Servicio>,

    @JsonBackReference
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "listaSubTipoEvento")
    private val listaExtraSubTipoEvento: Set<ExtraSubTipoEvento>,

    @JsonBackReference
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "listaSubTipoEvento")
    private val listaExtraVariableSubTipoEvento: Set<ExtraVariableSubTipoEvento>,

    @JsonBackReference
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "listaSubTipoEvento")
    private val listaExtraVariableCatering: Set<ExtraVariableCatering>,

    @JsonBackReference
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "listaSubTipoEvento")
    private val listaTipoCatering: Set<TipoCatering>){}