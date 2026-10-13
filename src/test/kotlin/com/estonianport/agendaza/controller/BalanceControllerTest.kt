package com.estonianport.agendaza.controller

import com.estonianport.agendaza.common.poiExcel.PoiExcelService
import com.estonianport.agendaza.service.BalanceService
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.server.ResponseStatusException
import java.time.YearMonth

class BalanceControllerTest {
    private val balanceService = mock<BalanceService>()
    private val poiExcelService = mock<PoiExcelService>()
    private val controller = BalanceController(balanceService, poiExcelService)

    @Test
    fun `descargar balance devuelve PDF como adjunto`() {
        val pdf = byteArrayOf(1, 2, 3)
        whenever(balanceService.generarBalancePdf(7L, 1, 2026, 2, 2026)).thenReturn(pdf)

        val response = controller.descargarBalance(7L, 1, 2026, 2, 2026)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(MediaType.APPLICATION_PDF, response.headers.contentType)
        assertEquals("attachment; filename=\"balance_2026-01_a_2026-02.pdf\"", response.headers[HttpHeaders.CONTENT_DISPOSITION]?.first())
        assertArrayEquals(pdf, response.body)
    }

    @Test
    fun `descargar planilla devuelve Excel y convierte rango invalido a bad request`() {
        val xlsx = byteArrayOf(4, 5, 6)
        whenever(poiExcelService.generarPlanilla(7L, YearMonth.of(2026, 1), YearMonth.of(2026, 2))).thenReturn(xlsx)

        val response = controller.descargarPlanilla(7L, 1, 2026, 2, 2026)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", response.headers.contentType?.toString())
        assertEquals("attachment; filename=\"planilla_2026-01_a_2026-02.xlsx\"", response.headers[HttpHeaders.CONTENT_DISPOSITION]?.first())
        assertArrayEquals(xlsx, response.body)
        verify(poiExcelService).generarPlanilla(7L, YearMonth.of(2026, 1), YearMonth.of(2026, 2))

        whenever(poiExcelService.generarPlanilla(eq(7L), any(), any())).thenThrow(IllegalArgumentException("rango inválido"))
        val error = assertThrows(ResponseStatusException::class.java) {
            controller.descargarPlanilla(7L, 1, 2026, 2, 2026)
        }
        assertEquals(HttpStatus.BAD_REQUEST, error.statusCode)
    }
}
