package com.estonianport.agendaza.common.security

import com.estonianport.agendaza.common.GenericServiceImpl
import com.estonianport.agendaza.model.PrecioConFechaExtra
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.repository.CrudRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant

@Service
class RefreshTokenService(
    val refreshTokenRepository: RefreshTokenRepository
) : GenericServiceImpl<RefreshToken, Long>(){

    override val dao: CrudRepository<RefreshToken, Long>
        get() = refreshTokenRepository

    companion object {
        const val MAX_SESIONES = 4
        val DURACION: Duration = Duration.ofDays(30)
    }

    @Transactional
    fun crear(username: String): RefreshToken {
        val activos = refreshTokenRepository.findByUsernameOrderByExpiryDateAsc(username)
        if (activos.size >= MAX_SESIONES) {
            refreshTokenRepository.deleteAll(activos.take(activos.size - MAX_SESIONES + 1))
        }
        return refreshTokenRepository.save(RefreshToken(username = username, expiryDate = Instant.now().plus(DURACION)))
    }

    /** Consume el token y devuelve uno nuevo; null si no existe o está vencido. */
    @Transactional
    fun rotar(tokenActual: String): RefreshToken? {
        val stored = refreshTokenRepository.findByToken(tokenActual) ?: return null
        refreshTokenRepository.delete(stored)
        if (stored.expiryDate.isBefore(Instant.now())) return null
        return refreshTokenRepository.save(RefreshToken(username = stored.username, expiryDate = Instant.now().plus(DURACION)))
    }

    fun revocar(token: String) {
        refreshTokenRepository.deleteByToken(token)
    }

    @Scheduled(cron = "0 0 3 * * *")
    fun limpiarVencidos() {
        refreshTokenRepository.deleteByExpiryDateBefore(Instant.now())
    }

}
