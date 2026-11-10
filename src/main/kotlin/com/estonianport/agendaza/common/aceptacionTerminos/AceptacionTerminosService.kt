package com.estonianport.agendaza.common.aceptacionTerminos

import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.security.MessageDigest

@Service
class TerminosService(
    private val aceptacionTerminosRepository: AceptacionTerminosRepository
) {

    companion object { const val VERSION_VIGENTE = "2026-11-01" }

    // El texto vive en src/main/resources/terminos/x.md
    private val texto: String by lazy {
        ClassPathResource("terminos/$VERSION_VIGENTE.md").inputStream.readBytes().toString(Charsets.UTF_8)
    }
    private val hash: String by lazy {
        MessageDigest.getInstance("SHA-256").digest(texto.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    fun vigente() = TerminosDTO(VERSION_VIGENTE, texto, hash)

    fun aceptoVigente(usuarioId: Long) = aceptacionTerminosRepository.existsByUsuarioIdAndVersion(usuarioId, VERSION_VIGENTE)

    fun registrarAceptacion(usuarioId: Long, ip: String?, userAgent: String?) {
        aceptacionTerminosRepository.save(
            AceptacionTerminos(
                usuarioId = usuarioId, version = VERSION_VIGENTE,
                hashTexto = hash, ip = ip, userAgent = userAgent?.take(512)
            )
        )
    }
}