package com.estonianport.agendaza.common.codeGeneratorUtil

import java.util.Random
import java.util.UUID

object CodeGeneratorUtil {

    /**
     * Genera un código aleatorio de 4 letras mayúsculas (Base26).
     */
    fun generarCodigoBase26(cantidad : Int): String {
        val random = Random()
        val base26chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()
        val sb = StringBuilder(cantidad)
        for (i in 0..3) {
            sb.append(base26chars[random.nextInt(base26chars.size)])
        }
        return sb.toString()
    }

    /**
     * Genera un número fantasma 999XXXXXXX.
     */
    fun generarCelularUnico(): Long {
        return "999${System.currentTimeMillis().toString().takeLast(7)}".toLong()
    }

    /**
     * Devuelve el email provisto o genera uno único ficticio si viene en blanco.
     */
    fun generarEmailUnico(): String {
        return "sin-email-${UUID.randomUUID()}@agendaza.com"
    }
}