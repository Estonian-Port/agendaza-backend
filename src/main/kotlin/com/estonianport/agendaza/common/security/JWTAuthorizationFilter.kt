package com.estonianport.agendaza.common.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JWTAuthorizationFilter : OncePerRequestFilter() {

    companion object {
        const val ATTR_TOKEN_ERROR = "jwt.token.error"
        const val TOKEN_EXPIRED = "EXPIRED"
        const val TOKEN_INVALID = "INVALID"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val bearerToken : String? = request.getHeader("Authorization")

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            val token : String = bearerToken.replace("Bearer ", "")
            val usernamePAT : UsernamePasswordAuthenticationToken? = TokenUtils.getAuthentication(token)
            if (usernamePAT != null) {
                SecurityContextHolder.getContext().authentication = usernamePAT
            } else {
                request.setAttribute(
                    ATTR_TOKEN_ERROR,
                    if (TokenUtils.isExpired(token)) TOKEN_EXPIRED else TOKEN_INVALID
                )
            }
        }

        filterChain.doFilter(request, response)
    }

}
