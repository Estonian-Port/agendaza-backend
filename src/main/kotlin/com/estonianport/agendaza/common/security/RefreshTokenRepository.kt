package com.estonianport.agendaza.common.security

import org.springframework.data.repository.CrudRepository

interface RefreshTokenRepository : CrudRepository<RefreshToken, Long>{
    fun findByToken(token: String): RefreshToken?
}
