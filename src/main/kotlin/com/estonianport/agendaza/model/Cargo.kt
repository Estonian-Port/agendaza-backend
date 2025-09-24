package com.estonianport.agendaza.model

import com.estonianport.agendaza.model.enums.TipoCargo
import jakarta.persistence.*
import java.time.LocalDate

@Entity
class Cargo(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @ManyToOne
    @PrimaryKeyJoinColumn
    var usuario: Usuario,

    @ManyToOne
    @PrimaryKeyJoinColumn
    var empresa: Empresa,

    @PrimaryKeyJoinColumn
    @Enumerated(EnumType.STRING)
    var tipoCargo : TipoCargo,

        @Column
    var fechaBaja : LocalDate? = null)