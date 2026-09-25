package com.estonianport.agendaza.common.codeGeneratorUtil

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CodeGeneratorUtilTest {

    @Test
    fun `base26Only4Letters genera cuatro letras mayusculas`() {
        val codigo = CodeGeneratorUtil.base26Only4Letters

        assertEquals(4, codigo.length)
        assertTrue(codigo.all { it in 'A'..'Z' })
    }
}
