package com.estonianport.agendaza.common.security

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

/**
 * Controla los intentos fallidos de login usando Redis (contadores con TTL).
 *
 * - Por IP: frena a quien prueba muchos usuarios desde la misma máquina.
 * - Por username: frena a quien ataca una cuenta puntual desde muchas IPs.
 *
 * Si Redis no está disponible, el login sigue funcionando (fail-open) y se loguea el error.
 */
@Service
class LoginAttemptService(private val redis: StringRedisTemplate) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val MAX_INTENTOS_USUARIO = 5
        const val MAX_INTENTOS_IP = 20
        val VENTANA: Duration = Duration.ofMinutes(15)

        private const val MAX_LARGO_USERNAME = 100
    }

    private fun keyUsuario(username: String) =
        "login:fail:user:${username.lowercase().take(MAX_LARGO_USERNAME)}"

    private fun keyIp(ip: String) = "login:fail:ip:$ip"

    fun estaBloqueado(ip: String, username: String?): Boolean =
        leerSeguro(false) {
            superaLimite(keyIp(ip), MAX_INTENTOS_IP) ||
                    (username != null && superaLimite(keyUsuario(username), MAX_INTENTOS_USUARIO))
        }

    fun registrarFallo(ip: String, username: String?) {
        log.debug("Login fallido ip={} username={}", ip, sanitizar(username))

        ejecutarSeguro {
            // Se loguea solo al cruzar el umbral: una línea por bloqueo, no por intento
            val intentosIp = incrementar(keyIp(ip))
            if (intentosIp == MAX_INTENTOS_IP.toLong()) {
                log.warn("IP bloqueada por {} intentos de login fallidos: ip={}", MAX_INTENTOS_IP, ip)
            }

            if (username != null) {
                val intentosUsuario = incrementar(keyUsuario(username))
                if (intentosUsuario == MAX_INTENTOS_USUARIO.toLong()) {
                    log.warn(
                        "Usuario bloqueado por {} intentos de login fallidos: username={} ip={}",
                        MAX_INTENTOS_USUARIO, sanitizar(username), ip
                    )
                }
            }
        }
    }

    fun registrarExito(username: String?) {
        ejecutarSeguro {
            if (username != null) redis.delete(keyUsuario(username))
        }
    }

    private fun superaLimite(key: String, max: Int): Boolean =
        (redis.opsForValue().get(key)?.toIntOrNull() ?: 0) >= max

    private fun incrementar(key: String): Long {
        val valor = redis.opsForValue().increment(key) ?: 0L
        // Cada fallo renueva el TTL: el bloqueo dura VENTANA desde el último fallo
        redis.expire(key, VENTANA)
        return valor
    }

    // El username lo controla el atacante: se corta el largo y se sacan saltos de línea (log injection)
    private fun sanitizar(username: String?): String? =
        username?.take(50)?.replace(Regex("[\\r\\n]"), "_")

    private fun <T> leerSeguro(default: T, block: () -> T): T =
        try {
            block()
        } catch (e: Exception) {
            log.error("Redis no disponible en el control de intentos de login", e)
            default
        }

    private fun ejecutarSeguro(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            log.error("Redis no disponible en el control de intentos de login", e)
        }
    }
}