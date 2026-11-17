package com.estonianport.agendaza.common.security

import org.springframework.data.repository.CrudRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

interface RefreshTokenRepository : CrudRepository<RefreshToken, Long> {

    fun findByToken(token: String): RefreshToken?

    fun findByUsernameOrderByExpiryDateAsc(username: String): List<RefreshToken>

    @Transactional
    fun deleteByToken(token: String): Long

    @Transactional
    fun deleteByUsername(username: String): Long

    @Modifying
    @Transactional
    @Query("update RefreshToken r set r.username = :nuevoUsername where r.username = :username")
    fun actualizarUsername(username: String, nuevoUsername: String): Int

    @Transactional
    fun deleteByExpiryDateBefore(fecha: Instant): Long
}
