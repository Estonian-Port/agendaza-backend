package com.estonianport.agendaza.common.aceptacionTerminos

import com.estonianport.agendaza.model.Usuario
import com.estonianport.agendaza.repository.UsuarioRepository
import jakarta.servlet.FilterChain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class OnboardingFilterTest {
    private val onboardingService = mock<OnboardingService>()
    private val usuarioRepository = mock<UsuarioRepository>()
    private val filter = OnboardingFilter(onboardingService, usuarioRepository)

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `permite completar datos mientras se requiere cambio de password`() {
        val usuario = Usuario(1L, "Ana", "Perez", 123L, "ana@test.com").apply {
            username = "ana"
            debeCambiarPassword = true
        }
        whenever(usuarioRepository.getByUsername("ana")).thenReturn(usuario)
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken("ana", null, emptyList())

        val request = MockHttpServletRequest("PUT", "/v1/usuarios/me/completar-datos")
        val response = MockHttpServletResponse()
        var chainReached = false
        val chain = FilterChain { _, _ -> chainReached = true }

        filter.doFilter(request, response, chain)

        assertTrue(chainReached)
        assertEquals(200, response.status)
    }
}