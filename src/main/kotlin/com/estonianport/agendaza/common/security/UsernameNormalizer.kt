package com.estonianport.agendaza.common.security

object UsernameNormalizer {
    fun normalize(username: String): String = username.trim().lowercase()
}
