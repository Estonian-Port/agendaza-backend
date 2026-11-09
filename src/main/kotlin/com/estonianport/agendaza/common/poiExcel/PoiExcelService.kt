package com.estonianport.agendaza.common.poiExcel

import com.estonianport.agendaza.model.Gasto
import com.estonianport.agendaza.model.Pago
import com.estonianport.agendaza.model.enums.TipoGasto
import com.estonianport.agendaza.repository.GastoRepository
import com.estonianport.agendaza.repository.PagoRepository
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.CellReference
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

/**
 * Planilla de movimientos en Excel: una hoja por mes ("OCTUBRE 26") con 4 tablas
 * (Eventos, Pagos, Reinversión, Gastos fijos) y un resumen al pie, como SAVEUR DIARIO 2026.
 *
 * Clasificación de gastos (todos los gastos del mes caen en exactamente una tabla):
 *  - Eventos:     gasto vinculado a un evento (cualquier tipo)
 *  - Reinversión: sin evento y tipo MANTENIMIENTO / DECORACION / EQUIPAMIENTO
 *  - Gastos fijos: el resto sin evento (SERVICIOS, IMPUESTOS, SUELDOS, TARJETA_CREDITO, OTROS, ...)
 */
@Service
class PoiExcelService(
    private val pagoRepository: PagoRepository,
    private val gastoRepository: GastoRepository
) {

    private companion object { const val MAX_MESES = 36L }

    private val meses = listOf(
        "ENERO", "FEBRERO", "MARZO", "ABRIL", "MAYO", "JUNIO",
        "JULIO", "AGOSTO", "SEPTIEMBRE", "OCTUBRE", "NOVIEMBRE", "DICIEMBRE"
    )

    private val tiposReinversion = setOf(
        TipoGasto.REINVERSION
        //TipoGasto.MANTENIMIENTO, TipoGasto.DECORACION, TipoGasto.EQUIPAMIENTO
    )

    private data class Col(val nombre: String, val ancho: Int)
    private class Tabla(val titulo: String, val columnas: List<Col>, val filas: List<List<Any?>>)

    // ==================== ENTRADA ====================

    @Transactional(readOnly = true)
    fun generarPlanilla(empresaId: Long, desde: YearMonth, hasta: YearMonth): ByteArray {
        require(!desde.isAfter(hasta)) { "El mes inicial no puede ser posterior al final" }
        require(desde.until(hasta, java.time.temporal.ChronoUnit.MONTHS) < MAX_MESES) { "El rango no puede superar $MAX_MESES meses" }
        val desdeFecha = desde.atDay(1).atStartOfDay()
        val hastaFecha = hasta.plusMonths(1).atDay(1).atStartOfDay()   // exclusivo

        val pagos = pagoRepository.findParaPlanilla(empresaId, desdeFecha, hastaFecha)
        val gastos = gastoRepository.findParaPlanilla(empresaId, desdeFecha, hastaFecha)
        return construirLibro(pagos, gastos, desde, hasta)
    }

    fun construirLibro(pagos: List<Pago>, gastos: List<Gasto>, desde: YearMonth, hasta: YearMonth): ByteArray {
        val pagosPorMes = pagos.groupBy { YearMonth.from(it.fecha) }
        val gastosPorMes = gastos.groupBy { YearMonth.from(it.fecha) }

        XSSFWorkbook().use { wb ->
            val estilos = Estilos(wb)
            var mes = hasta                       // el más reciente primero, como la planilla original
            while (!mes.isBefore(desde)) {
                hojaMes(wb, estilos, mes, pagosPorMes[mes].orEmpty(), gastosPorMes[mes].orEmpty())
                mes = mes.minusMonths(1)
            }
            wb.creationHelper.createFormulaEvaluator().evaluateAll()   // deja valores cacheados en las fórmulas
            val baos = ByteArrayOutputStream()
            wb.write(baos)
            return baos.toByteArray()
        }
    }

    // ==================== HOJA ====================

    private fun hojaMes(wb: Workbook, e: Estilos, mes: YearMonth, pagos: List<Pago>, gastos: List<Gasto>) {
        val sheet = wb.createSheet("${meses[mes.monthValue - 1]} ${(mes.year % 100).toString().padStart(2, '0')}")

        val (gastosEvento, sinEvento) = gastos.partition { it.evento != null }
        val (reinversion, fijos) = sinEvento.partition { it.tipoGasto in tiposReinversion }

        val tEventos = Tabla(
            "EVENTOS",
            listOf(Col("Fecha", 18), Col("Evento", 34), Col("Importe", 18)),
            emptyList()
        )

        val tPagos = Tabla(
            "PAGOS",
            listOf(
                Col("Fecha evento", 13), Col("Importe", 15), Col("Fecha cobro", 13), Col("Forma de pago", 18),
                Col("Factura", 9), Col("Cliente", 24)
            ),
            pagos.sortedBy { it.fecha }.map { p ->
                listOf(
                    p.evento.inicio, p.monto, p.fecha, formatEnum(p.medioDePago),
                    formatEnum(p.concepto) + (p.numeroCuota?.let { " (cuota $it)" } ?: ""),
                    nombreCliente(p)
                )
            }
        )

        val tReinversion = Tabla(
            "REINVERSIÓN",
            listOf(Col("Fecha", 12), Col("Descripción", 32), Col("Importe", 15)),
            reinversion.sortedBy { it.fecha }.map { listOf(it.fecha, it.descripcion, it.monto) }
        )

        val tFijos = Tabla(
            "GASTOS FIJOS",
            listOf(Col("Fecha", 12), Col("Tipo", 16), Col("Concepto", 32), Col("Importe", 15)),
            fijos.sortedBy { it.fecha }.map { listOf(it.fecha, formatEnum(it.tipoGasto), it.descripcion, it.monto) }
        )

        // Las tablas van una al lado de otra con una columna vacía de separación
        var col = 0
        val refs = mutableListOf<String>()
        refs += escribirTablaEventos(sheet, e, col, gastosEvento, pagos, mes)
        col += tEventos.columnas.size + 1
        for (t in listOf(tPagos, tReinversion, tFijos)) {
            refs += escribirTabla(sheet, e, col, t)
            col += t.columnas.size + 1
        }
        val (refEventos, refPagos, refReinversion, refFijos) = refs

        val filasEventos = cantidadFilasEventos(gastosEvento, pagos, mes)
        val maxFilas = maxOf(filasEventos, tPagos.filas.size, tReinversion.filas.size, tFijos.filas.size)
        escribirResumen(sheet, e, 2 + maxFilas + 3, refEventos, refPagos, refReinversion, refFijos)

        sheet.createFreezePane(0, 2)
    }

    /**
     * Escribe un bloque independiente por evento. Cada bloque termina con su total
     * de gastos y deja una fila libre antes del siguiente evento.
     */
    private fun escribirTablaEventos(
        sheet: Sheet, e: Estilos, col0: Int, gastos: List<Gasto>, pagos: List<Pago>, mes: YearMonth
    ): String {
        val n = 3
        sheet.addMergedRegion(CellRangeAddress(0, 0, col0, col0 + n - 1))
        for (i in 0 until n) celda(sheet, 0, col0 + i).cellStyle = e.titulo
        celda(sheet, 0, col0).setCellValue("EVENTOS")

        listOf(18, 34, 18).forEachIndexed { i, ancho -> sheet.setColumnWidth(col0 + i, ancho * 256) }

        val eventos = (gastos.mapNotNull { it.evento } + pagos.map { it.evento })
            .distinctBy { it.id }
            .filter { YearMonth.from(it.inicio) == mes }
            .sortedBy { it.inicio }
        val gastosPorEvento = gastos.groupBy { it.evento?.id }
        val referenciasTotales = mutableListOf<String>()
        var fila = 1

        eventos.forEach { evento ->
            // La fila naranja identifica visualmente el comienzo de cada evento.
            for (i in 0 until n) celda(sheet, fila, col0 + i).cellStyle = e.evento
            celda(sheet, fila, col0).apply { setCellValue(evento.inicio); cellStyle = e.eventoFecha }
            celda(sheet, fila, col0 + 1).apply { setCellValue(evento.nombre); cellStyle = e.evento }
            fila++

            sheet.addMergedRegion(CellRangeAddress(fila, fila, col0, col0 + 1))
            celda(sheet, fila, col0).apply { setCellValue("Cantidad de invitados"); cellStyle = e.totalEtiqueta }
            celda(sheet, fila, col0 + 2).apply {
                setCellValue("${evento.capacidadAdultos} adultos + ${evento.capacidadNinos} menores")
                cellStyle = e.texto
            }
            fila++

            sheet.addMergedRegion(CellRangeAddress(fila, fila, col0, col0 + 1))
            celda(sheet, fila, col0).apply { setCellValue("Total de ingresos"); cellStyle = e.ingresoEtiqueta }
            celda(sheet, fila, col0 + 2).apply { setCellValue(evento.getPresupuestoTotal()); cellStyle = e.ingreso }
            fila++

            listOf("Tipo", "Descripción", "Cantidad").forEachIndexed { i, nombre ->
                celda(sheet, fila, col0 + i).apply { setCellValue(nombre); cellStyle = e.encabezado }
            }
            fila++

            val gastosEvento = gastosPorEvento[evento.id].orEmpty().sortedBy { it.fecha }
            gastosEvento.forEach { gasto ->
                celda(sheet, fila, col0).apply { setCellValue(formatEnum(gasto.tipoGasto)); cellStyle = e.texto }
                celda(sheet, fila, col0 + 1).apply { setCellValue(gasto.descripcion); cellStyle = e.texto }
                celda(sheet, fila, col0 + 2).apply { setCellValue(gasto.monto); cellStyle = e.moneda }
                fila++
            }

            val totalGasto = gastosEvento.sumOf { it.monto }
            sheet.addMergedRegion(CellRangeAddress(fila, fila, col0, col0 + 1))
            celda(sheet, fila, col0).apply { setCellValue("TOTAL GASTO"); cellStyle = e.gastoEtiqueta }
            celda(sheet, fila, col0 + 2).apply { setCellValue(totalGasto); cellStyle = e.gasto }
            referenciasTotales += CellReference(fila, col0 + 2).formatAsString()
            fila += 2
        }

        val filaTotal = if (eventos.isEmpty()) 1 else fila - 1
        val celdaTotal = celda(sheet, filaTotal, col0 + 2)
        if (eventos.isEmpty()) {
            sheet.addMergedRegion(CellRangeAddress(filaTotal, filaTotal, col0, col0 + 1))
            celda(sheet, filaTotal, col0).apply { setCellValue("TOTAL GASTO"); cellStyle = e.gastoEtiqueta }
            celdaTotal.setCellValue(0.0)
            celdaTotal.cellStyle = e.gasto
        }
        return if (referenciasTotales.isEmpty()) {
            CellReference(filaTotal, col0 + 2).formatAsString()
        } else {
            "SUM(${referenciasTotales.joinToString(",")})"
        }
    }

    private fun cantidadFilasEventos(gastos: List<Gasto>, pagos: List<Pago>, mes: YearMonth): Int {
        val eventos = (gastos.mapNotNull { it.evento } + pagos.map { it.evento })
            .distinctBy { it.id }
            .filter { YearMonth.from(it.inicio) == mes }
        return eventos.sumOf { evento -> 6 + gastos.count { it.evento?.id == evento.id } }
    }

    /** Devuelve la referencia (ej. "E10") de la celda con el total de importes. */
    private fun escribirTabla(sheet: Sheet, e: Estilos, col0: Int, t: Tabla): String {
        val n = t.columnas.size

        sheet.addMergedRegion(CellRangeAddress(0, 0, col0, col0 + n - 1))
        for (i in 0 until n) celda(sheet, 0, col0 + i).cellStyle = e.titulo
        celda(sheet, 0, col0).setCellValue(t.titulo)

        t.columnas.forEachIndexed { i, c ->
            celda(sheet, 1, col0 + i).apply { setCellValue(c.nombre); cellStyle = e.encabezado }
            sheet.setColumnWidth(col0 + i, c.ancho * 256)
        }

        t.filas.forEachIndexed { r, fila ->
            fila.forEachIndexed { i, v ->
                val c = celda(sheet, 2 + r, col0 + i)
                when (v) {
                    null -> c.cellStyle = e.texto
                    is LocalDateTime -> { c.setCellValue(v); c.cellStyle = e.fecha }
                    is LocalDate -> { c.setCellValue(v); c.cellStyle = e.fecha }
                    is Double -> { c.setCellValue(v); c.cellStyle = e.moneda }
                    else -> { c.setCellValue(v.toString()); c.cellStyle = e.texto }
                }
            }
        }

        val filaTotal = 2 + t.filas.size
        val iImporte = t.columnas.indexOfFirst { it.nombre == "Importe" }
        for (i in 0 until n) celda(sheet, filaTotal, col0 + i).cellStyle = e.totalEtiqueta
        celda(sheet, filaTotal, col0).setCellValue("TOTAL")

        val celdaTotal = celda(sheet, filaTotal, col0 + iImporte)
        celdaTotal.cellStyle = e.total
        if (t.filas.isEmpty()) {
            celdaTotal.setCellValue(0.0)
        } else {
            val letra = CellReference.convertNumToColString(col0 + iImporte)
            celdaTotal.cellFormula = "SUM(${letra}3:${letra}$filaTotal)"
        }
        return CellReference(filaTotal, col0 + iImporte).formatAsString()
    }

    private fun escribirResumen(
        sheet: Sheet, e: Estilos, fila0: Int,
        refEventos: String, refPagos: String, refReinversion: String, refFijos: String
    ) {
        // Etiquetas en la columna B y valores en la C (fila de Excel = índice + 1)
        fun linea(i: Int, etiqueta: String, formula: String) {
            celda(sheet, fila0 + i, 1).apply { setCellValue(etiqueta); cellStyle = e.totalEtiqueta }
            celda(sheet, fila0 + i, 2).apply { cellFormula = formula; cellStyle = e.total }
        }
        celda(sheet, fila0 - 1, 1).apply { setCellValue("RESUMEN"); cellStyle = e.encabezado }
        celda(sheet, fila0 - 1, 2).cellStyle = e.encabezado

        linea(0, "Gasto eventos", refEventos)
        linea(1, "Reinversión", refReinversion)
        linea(2, "Gastos fijos", refFijos)
        linea(3, "Gasto total", "SUM(C${fila0 + 1}:C${fila0 + 3})")
        linea(4, "Ingresos", refPagos)
        linea(5, "Resultado", "C${fila0 + 5}-C${fila0 + 4}")
    }

    // ==================== UTILIDADES ====================

    private fun celda(sheet: Sheet, fila: Int, col: Int) =
        (sheet.getRow(fila) ?: sheet.createRow(fila)).let { it.getCell(col) ?: it.createCell(col) }

    private fun formatEnum(e: Enum<*>): String =
        e.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }

    private fun nombreCliente(p: Pago): String = "${p.evento.cliente.nombre} ${p.evento.cliente.apellido}"

    private class Estilos(wb: Workbook) {
        private val formato = wb.creationHelper.createDataFormat()

        private fun gris(s: CellStyle, color: IndexedColors) {
            s.fillForegroundColor = color.index
            s.fillPattern = FillPatternType.SOLID_FOREGROUND
        }

        val titulo: CellStyle = wb.createCellStyle().apply {
            gris(this, IndexedColors.ROYAL_BLUE)
            alignment = HorizontalAlignment.CENTER
            setFont(wb.createFont().apply { bold = true; color = IndexedColors.WHITE.index })
        }
        val encabezado: CellStyle = wb.createCellStyle().apply {
            gris(this, IndexedColors.GREY_25_PERCENT)
            alignment = HorizontalAlignment.CENTER
            borderBottom = BorderStyle.THIN
            setFont(wb.createFont().apply { bold = true })
        }
        val texto: CellStyle = wb.createCellStyle()
        val fecha: CellStyle = wb.createCellStyle().apply {
            dataFormat = formato.getFormat("dd/mm/yyyy")
            alignment = HorizontalAlignment.CENTER
        }
        val moneda: CellStyle = wb.createCellStyle().apply { dataFormat = formato.getFormat("#,##0") }
        val totalEtiqueta: CellStyle = wb.createCellStyle().apply {
            gris(this, IndexedColors.GREY_25_PERCENT)
            borderTop = BorderStyle.THIN
            setFont(wb.createFont().apply { bold = true })
        }
        val total: CellStyle = wb.createCellStyle().apply {
            gris(this, IndexedColors.GREY_25_PERCENT)
            borderTop = BorderStyle.THIN
            dataFormat = formato.getFormat("#,##0")
            setFont(wb.createFont().apply { bold = true })
        }
        val evento: CellStyle = wb.createCellStyle().apply {
            gris(this, IndexedColors.ORANGE)
            setFont(wb.createFont().apply { bold = true })
        }
        val eventoFecha: CellStyle = wb.createCellStyle().apply {
            gris(this, IndexedColors.ORANGE)
            dataFormat = formato.getFormat("dd/mm/yyyy")
            setFont(wb.createFont().apply { bold = true })
        }
        val ingresoEtiqueta: CellStyle = wb.createCellStyle().apply {
            gris(this, IndexedColors.LIGHT_GREEN)
            borderTop = BorderStyle.THIN
            setFont(wb.createFont().apply { bold = true })
        }
        val ingreso: CellStyle = wb.createCellStyle().apply {
            gris(this, IndexedColors.LIGHT_GREEN)
            borderTop = BorderStyle.THIN
            dataFormat = formato.getFormat("#,##0")
            setFont(wb.createFont().apply { bold = true })
        }
        val gastoEtiqueta: CellStyle = wb.createCellStyle().apply {
            gris(this, IndexedColors.ROSE)
            borderTop = BorderStyle.THIN
            setFont(wb.createFont().apply { bold = true })
        }
        val gasto: CellStyle = wb.createCellStyle().apply {
            gris(this, IndexedColors.ROSE)
            borderTop = BorderStyle.THIN
            dataFormat = formato.getFormat("#,##0")
            setFont(wb.createFont().apply { bold = true })
        }
    }
}
