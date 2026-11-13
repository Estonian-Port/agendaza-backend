package com.estonianport.agendaza.common.security

object UsernameNormalizer {
    @JvmStatic
    fun normalize(username: String): String = username.trim().lowercase()
}
