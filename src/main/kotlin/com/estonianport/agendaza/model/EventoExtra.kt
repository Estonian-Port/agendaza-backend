package com.estonianport.agendaza.model

import jakarta.persistence.*

@Entity
@Table(name = "evento_extra")
class EventoExtra(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    var evento: Evento,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "extra_id", nullable = false)
    var extra: Extra,

    @Column(nullable = false)
    var precio: Double,

    @Column(nullable = false)
    var cantidad: Int = 1
) {
    val subtotal: Double
        get() = precio * cantidad
}