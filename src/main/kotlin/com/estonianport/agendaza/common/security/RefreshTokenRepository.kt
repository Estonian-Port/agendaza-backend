package com.estonianport.agendaza.common.security

import org.springframework.data.repository.CrudRepository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

interface RefreshTokenRepository : CrudRepository<RefreshToken, Long> {

    fun findByToken(token: String): RefreshToken?

    fun findByUsernameOrderByExpiryDateAsc(username: String): List<RefreshToken>

    @Transactional
    fun deleteByToken(token: String): Long

    @Transactional
    fun deleteByUsername(username: String): Long

    @Transactional
    fun deleteByExpiryDateBefore(fecha: Instant): Long
}
