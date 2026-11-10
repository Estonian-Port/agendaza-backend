package com.estonianport.agendaza.common.aceptacionTerminos

import com.estonianport.agendaza.repository.UsuarioRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OnboardingService(
    private val usuarioRepository: UsuarioRepository,
    private val terminosService: TerminosService
) {
    @Transactional(readOnly = true)
    @Cacheable(value = ["onboardingPendiente"], key = "#username")
    fun pendiente(username: String): Boolean {
        val u = usuarioRepository.getByUsername(username) ?: return false
        return !terminosService.aceptoVigente(u.id)
    }
}