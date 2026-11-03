package com.estonianport.agendaza.common.security

import com.estonianport.agendaza.common.aceptacionTerminos.OnboardingFilter
import com.estonianport.agendaza.common.aceptacionTerminos.OnboardingService
import com.estonianport.agendaza.repository.UsuarioRepository
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val jwtAuthorizationFilter: JWTAuthorizationFilter,
    private val loginAttemptService: LoginAttemptService,
    private val usuarioRepository: UsuarioRepository,
    private val onboardingService: OnboardingService
) {

    @Bean
    fun filterChain(http: HttpSecurity, authenticationManager: AuthenticationManager): SecurityFilterChain {
        val jwtAuthenticationFilter = JWTAuthenticationFilter(loginAttemptService, usuarioRepository)
        jwtAuthenticationFilter.setAuthenticationManager(authenticationManager)
        jwtAuthenticationFilter.setFilterProcessesUrl("/login")
        jwtAuthenticationFilter.setAuthenticationFailureHandler(CustomAuthenticationFailureHandler(loginAttemptService))

        return http
            .cors(Customizer.withDefaults())
            .csrf { csrf -> csrf.disable() }
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers("/", "/index.html", "/styles.css", "/logo.png", "/favicon.ico").permitAll()
                    .requestMatchers("/version/latest").permitAll()
                    .requestMatchers("/actuator/prometheus").access(WebExpressionAuthorizationManager("hasIpAddress('127.0.0.1') or hasIpAddress('::1')"))
                    .anyRequest().authenticated()
            }
            .exceptionHandling { ex ->
                ex.authenticationEntryPoint { _, res, _ -> res.json(401, "UNAUTHENTICATED", "Sesión inválida o vencida") }
                ex.accessDeniedHandler { _, res, _ -> res.json(403, "FORBIDDEN", "No tenés permiso para realizar esta acción") }
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .addFilter(jwtAuthenticationFilter)
            .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterAfter(OnboardingFilter(onboardingService), JWTAuthorizationFilter::class.java)
            .build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager {
        return authenticationConfiguration.authenticationManager
    }

    fun HttpServletResponse.json(status: Int, code: String, message: String) {
        this.status = status
        contentType = "application/json;charset=UTF-8"
        writer.write("""{"code":"$code","message":"$message","status":$status}""")
    }
}
