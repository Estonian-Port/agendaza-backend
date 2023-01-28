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


    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        var bearerToken : String = request.getHeader("Authorization")

        if (bearerToken.startsWith("Bearer ")) {
            var token : String = bearerToken.replace("Bearer ", "")
            var usernamePAT : UsernamePasswordAuthenticationToken? = TokenUtils.getAuthentication(token)
            SecurityContextHolder.getContext().authentication = usernamePAT
        }
        filterChain.doFilter(request, response)

    }

}