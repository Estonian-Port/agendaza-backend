package com.estonianport.agendaza.common.aceptacionTerminos

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import com.estonianport.agendaza.repository.UsuarioRepository
import com.estonianport.agendaza.common.security.UsernameNormalizer

class OnboardingFilter(
    private val onboardingService: OnboardingService,
    private val usuarioRepository: UsuarioRepository
) : OncePerRequestFilter() {

    private val permitidosOnboarding = Regex("^/(v1|api)/(usuarios/(me(/.*)?|check-username)|terminos/vigente)$")
    private val permitidosPassword = Regex("^/(v1|api)/usuarios/(me|me/estado|me/password|me/completar-datos)$")
    private val permitidosEmail = Regex("^/(v1|api)/usuarios/(me/estado|verificacion-email|verificacion-email/confirmar)$")
    private val permitidosLogout = Regex("^/(v1|api)/auth/logout$")

    override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth != null && auth.isAuthenticated && auth !is AnonymousAuthenticationToken
            && req.method != "OPTIONS"
            && !permitidosOnboarding.matches(req.requestURI)
            && !permitidosEmail.matches(req.requestURI)
            && !permitidosLogout.matches(req.requestURI)
            && onboardingService.pendiente(auth.name)
        ) {
            res.status = 403
            res.contentType = "application/json"
            res.writer.write("""{"code":"ONBOARDING_REQUIRED","message":"Debés completar tus datos y aceptar los términos"}""")
            return
        }

        if (auth != null && auth.isAuthenticated && auth !is AnonymousAuthenticationToken
            && req.method != "OPTIONS"
            && usuarioRepository.getByUsername(UsernameNormalizer.normalize(auth.name))?.emailVerificado != true
            && !permitidosEmail.matches(req.requestURI)
            && !permitidosOnboarding.matches(req.requestURI)
            && !permitidosPassword.matches(req.requestURI)
            && !permitidosLogout.matches(req.requestURI)
        ) {
            res.status = 403
            res.contentType = "application/json"
            res.writer.write("""{"code":"EMAIL_VERIFICATION_REQUIRED","message":"Debés verificar tu email"}""")
            return
        }

        if (auth != null && auth.isAuthenticated && auth !is AnonymousAuthenticationToken
            && req.method != "OPTIONS"
            && usuarioRepository.getByUsername(UsernameNormalizer.normalize(auth.name))?.debeCambiarPassword == true
            && !permitidosPassword.matches(req.requestURI)
            && !permitidosEmail.matches(req.requestURI)
            && !permitidosLogout.matches(req.requestURI)
        ) {
            res.status = 403
            res.contentType = "application/json"
            res.writer.write("""{"code":"PASSWORD_CHANGE_REQUIRED","message":"Debés cambiar tu contraseña"}""")
            return
        }
        chain.doFilter(req, res)
    }
}
