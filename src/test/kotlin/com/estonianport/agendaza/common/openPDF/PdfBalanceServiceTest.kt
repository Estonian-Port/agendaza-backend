package com.estonianport.agendaza.common.openPDF

import com.estonianport.agendaza.dto.BalanceReporte
import com.lowagie.text.pdf.PdfReader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.YearMonth

class PdfBalanceServiceTest {
    private val service = PdfBalanceService()

    @Test
    fun `genera un PDF legible con cuatro paginas para un rango sin logo`() {
        val bytes = service.generarBalance(
            BalanceReporte(
                empresaNombre = "Salon de prueba",
                logoUrl = null,
                desde = YearMonth.of(2026, 1),
                hasta = YearMonth.of(2026, 2),
                meses = emptyList(),
                gastosPorTipo = emptyMap(),
                casaDolar = "BCV"
            )
        )

        PdfReader(bytes).use { pdf ->
            assertEquals(4, pdf.numberOfPages)
            assertTrue((1..pdf.numberOfPages).all { pdf.getPageContent(it).isNotEmpty() })
        }
    }
}
