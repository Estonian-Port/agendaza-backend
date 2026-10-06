package com.estonianport.agendaza.model

import com.estonianport.agendaza.model.enums.MedioDePago
import com.estonianport.agendaza.model.enums.TipoGasto
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
class Gasto(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false)
    var monto: Double,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var tipoGasto: TipoGasto,

    @Column(length = 255, nullable = false)
    var descripcion: String,

    @Column(nullable = false)
    var fecha: LocalDateTime,

    @ManyToOne
    @JoinColumn(name = "evento_id")
    var evento: Evento? = null,

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    var empresa: Empresa,

    @ManyToOne
    @JoinColumn(name = "encargado_id", nullable = false)
    var encargado: Usuario,

    @Column
    @Enumerated(EnumType.STRING)
    var medioDePago: MedioDePago? = null,

    @Column
    var fechaBaja: LocalDate? = null
)