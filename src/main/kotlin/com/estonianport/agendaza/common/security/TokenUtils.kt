package com.estonianport.agendaza.common.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import java.util.*


class TokenUtils {

    companion object{

        val ACCESS_TOKEN_SECRET : String = Keys.secretKeyFor(SignatureAlgorithm.HS256).toString();
        val ACCESS_TOKEN_VALIDITY_SECONDS : Long = 259000

        fun createToken(username : String, email : String): String {
            var expirationTime : Long = ACCESS_TOKEN_VALIDITY_SECONDS * 1000
            var expirationDate : Date = Date(System.currentTimeMillis() + expirationTime)

            val extra: MutableMap<String, Any> = HashMap()
            extra["nombre"] = username

            return Jwts.builder()
                .setSubject(email)
                .setExpiration(expirationDate)
                .addClaims(extra)
                .signWith(Keys.hmacShaKeyFor((ACCESS_TOKEN_SECRET.toByteArray())))
                .compact()
        }

        fun getAuthentication(token : String) : UsernamePasswordAuthenticationToken? {

            try {
                var claims : Claims = Jwts.parserBuilder()
                    .setSigningKey(ACCESS_TOKEN_SECRET.toByteArray())
                    .build()
                    .parseClaimsJws(token)
                    .body

                var username : String = claims.subject

                return UsernamePasswordAuthenticationToken(username, null, Collections.emptyList())
            }catch (e : JwtException){
                return null
            }

        }
    }
}