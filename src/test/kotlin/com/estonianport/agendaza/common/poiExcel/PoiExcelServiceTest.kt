package com.estonianport.agendaza.common.poiExcel

import com.estonianport.agendaza.repository.GastoRepository
import com.estonianport.agendaza.repository.PagoRepository
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.ByteArrayInputStream
import java.time.YearMonth

class PoiExcelServiceTest {
    private val pagos = mock<PagoRepository>()
    private val gastos = mock<GastoRepository>()
    private val service = PoiExcelService(pagos, gastos)

    @Test
    fun `genera libro xlsx con una hoja mensual y encabezados de tablas`() {
        val bytes = service.construirLibro(emptyList(), emptyList(), YearMonth.of(2026, 1), YearMonth.of(2026, 2))

        XSSFWorkbook(ByteArrayInputStream(bytes)).use { workbook ->
            assertEquals(2, workbook.numberOfSheets)
            assertEquals("FEBRERO 26", workbook.getSheetName(0))
            assertEquals("ENERO 26", workbook.getSheetName(1))
            val sheet = workbook.getSheetAt(0)
            val texto = sheet.iterator().asSequence()
                .flatMap { row -> row.cellIterator().asSequence() }
                .filter { it.cellType == org.apache.poi.ss.usermodel.CellType.STRING }
                .map { it.stringCellValue }
                .filter(String::isNotBlank)
                .toSet()
            assertTrue("EVENTOS" in texto)
            assertTrue("PAGOS" in texto)
            assertTrue("REINVERSIÓN" in texto)
            assertTrue("GASTOS FIJOS" in texto)
        }
    }

    @Test
    fun `validar rango y consultar repositorios al generar planilla`() {
        whenever(pagos.findParaPlanilla(eq(3L), any(), any())).thenReturn(emptyList())
        whenever(gastos.findParaPlanilla(eq(3L), any(), any())).thenReturn(emptyList())

        val bytes = service.generarPlanilla(3L, YearMonth.of(2026, 1), YearMonth.of(2026, 1))

        assertTrue(bytes.isNotEmpty())
        verify(pagos).findParaPlanilla(eq(3L), any(), any())
        verify(gastos).findParaPlanilla(eq(3L), any(), any())
        assertThrows(IllegalArgumentException::class.java) {
            service.generarPlanilla(3L, YearMonth.of(2026, 2), YearMonth.of(2026, 1))
        }
        assertThrows(IllegalArgumentException::class.java) {
            service.generarPlanilla(3L, YearMonth.of(2023, 1), YearMonth.of(2026, 1))
        }
    }
}
