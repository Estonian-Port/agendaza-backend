package com.estonianport.agendaza.common.aceptacionTerminos

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class OnboardingFilter(private val onboardingService: OnboardingService) : OncePerRequestFilter() {

    private val permitidos = Regex("^/(v1|api)/(usuarios/(me(/.*)?|check-username)|terminos/vigente)$")

    override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth != null && auth.isAuthenticated && auth !is AnonymousAuthenticationToken
            && req.method != "OPTIONS"
            && !permitidos.matches(req.requestURI)
            && onboardingService.pendiente(auth.name)
        ) {
            res.status = 403
            res.contentType = "application/json"
            res.writer.write("""{"code":"ONBOARDING_REQUIRED","message":"Debés completar tus datos y aceptar los términos"}""")
            return
        }
        chain.doFilter(req, res)
    }
}