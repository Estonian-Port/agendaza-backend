package com.estonianport.agendaza.model

import jakarta.persistence.*
import java.time.LocalDate

@Entity
open class Cargo(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @ManyToOne
    @PrimaryKeyJoinColumn
    val usuario: Usuario,

    @ManyToOne
    @PrimaryKeyJoinColumn
    val empresa: Empresa,

    @PrimaryKeyJoinColumn
    @Enumerated(EnumType.STRING)
    var tipoCargo : TipoCargo,

    @Column
    var fechaBaja : LocalDate? = null){}