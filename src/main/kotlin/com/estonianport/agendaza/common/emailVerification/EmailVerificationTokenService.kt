package com.estonianport.agendaza.common.emailVerification

import com.estonianport.agendaza.common.codeGeneratorUtil.CodeGeneratorUtil
import com.estonianport.agendaza.errors.BadRequestException
import org.springframework.stereotype.Service
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
class EmailVerificationTokenService(
    private val repository: EmailVerificationTokenRepository
) {

    @Transactional
    fun crear(usuarioId: Long, email: String): String {
        val ahora = LocalDateTime.now()
        val anterior = repository.findByUsuarioId(usuarioId)
        if (anterior != null && anterior.enviadoEn.plusMinutes(1).isAfter(ahora)) {
            val segundos = ChronoUnit.SECONDS.between(ahora, anterior.enviadoEn.plusMinutes(1)).coerceAtLeast(1)
            throw BadRequestException("Podés solicitar otro código en $segundos segundos")
        }

        repository.deleteByUsuarioId(usuarioId)
        repository.flush()

        val codigo = CodeGeneratorUtil.generarCodigoBase26(4)
        repository.save(
            EmailVerificationToken(
                usuarioId = usuarioId,
                email = email,
                codigoHash = hash(codigo),
                creadoEn = ahora,
                venceEn = ahora.plusMinutes(10),
                enviadoEn = ahora
            )
        )
        return codigo
    }

    @Transactional
    fun validarYConsumir(usuarioId: Long, email: String, codigo: String) {
        val token = repository.findByUsuarioId(usuarioId)
        val codigoNormalizado = codigo.trim().uppercase()
        if (token == null || token.email != email || token.venceEn.isBefore(LocalDateTime.now()) ||
            !MessageDigest.isEqual(hash(codigoNormalizado).toByteArray(), token.codigoHash.toByteArray())
        ) {
            if (token != null && token.venceEn.isBefore(LocalDateTime.now())) {
                repository.delete(token)
            }
            throw BadRequestException("El código de verificación es inválido o venció")
        }

        repository.delete(token)
    }

    @Transactional
    fun eliminar(usuarioId: Long) {
        repository.deleteByUsuarioId(usuarioId)
    }

    @Transactional
    @Scheduled(cron = "0 15 3 * * *")
    fun eliminarVencidos() {
        repository.deleteByVenceEnBefore(LocalDateTime.now())
    }

    private fun hash(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}
