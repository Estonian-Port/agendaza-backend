package com.estonianport.agendaza.model

import jakarta.persistence.*
import org.hibernate.annotations.Proxy
import java.time.LocalDate

@Entity
@Proxy(lazy = false)
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