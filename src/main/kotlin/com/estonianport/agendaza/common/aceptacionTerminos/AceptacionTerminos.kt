package com.estonianport.agendaza.common.aceptacionTerminos

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Index
import java.time.LocalDateTime

@Entity
@Table(
    name = "aceptacion_terminos",
    indexes = [
        Index(name = "idx_aceptacion_usuario", columnList = "usuario_id,version")
    ]
)
class AceptacionTerminos(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "usuario_id", nullable = false)
    val usuarioId: Long,

    @Column(nullable = false)
    val version: String,

    @Column(nullable = false, length = 64)
    val hashTexto: String,

    @Column(nullable = false)
    val fechaAceptacion: LocalDateTime = LocalDateTime.now(),

    val ip: String?,

    val userAgent: String?
)
