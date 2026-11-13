package com.estonianport.agendaza.common.emailVerification

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface EmailVerificationTokenRepository : JpaRepository<EmailVerificationToken, Long> {
    fun findByUsuarioId(usuarioId: Long): EmailVerificationToken?
    fun deleteByUsuarioId(usuarioId: Long)
    fun deleteByVenceEnBefore(ahora: LocalDateTime): Long
}
