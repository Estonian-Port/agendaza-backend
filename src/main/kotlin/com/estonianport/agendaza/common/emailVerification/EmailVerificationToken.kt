package com.estonianport.agendaza.common.emailVerification

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(
    name = "email_verification_token",
    indexes = [
        Index(name = "idx_email_verification_usuario", columnList = "usuario_id", unique = true),
        Index(name = "idx_email_verification_expiry", columnList = "vence_en")
    ]
)
class EmailVerificationToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "usuario_id", nullable = false)
    val usuarioId: Long,

    @Column(nullable = false)
    val email: String,

    @Column(name = "codigo_hash", nullable = false, length = 64)
    val codigoHash: String,

    @Column(name = "creado_en", nullable = false)
    val creadoEn: LocalDateTime = LocalDateTime.now(),

    @Column(name = "vence_en", nullable = false)
    val venceEn: LocalDateTime,

    @Column(name = "enviado_en", nullable = false)
    val enviadoEn: LocalDateTime = creadoEn
)
