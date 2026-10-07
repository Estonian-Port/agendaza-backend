package com.estonianport.agendaza.common.openPDF

import com.estonianport.agendaza.dto.BalanceMes
import com.estonianport.agendaza.dto.BalanceReporte
import com.estonianport.agendaza.model.enums.TipoGasto
import com.lowagie.text.Document
import com.lowagie.text.Element
import com.lowagie.text.Font
import com.lowagie.text.Image
import com.lowagie.text.PageSize
import com.lowagie.text.Phrase
import com.lowagie.text.pdf.BaseFont
import com.lowagie.text.pdf.ColumnText
import com.lowagie.text.pdf.PdfContentByte
import com.lowagie.text.pdf.PdfWriter
import org.springframework.stereotype.Service
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.net.URL
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Balance financiero en PDF apaisado (A4 landscape) para proyectar.
 * Se dibuja directamente sobre el PdfContentByte (texto, barras, tarjetas),
 * así los gráficos son vectoriales y no hace falta ninguna librería extra.
 *
 * Páginas: 1) Resumen  2) Ingresos vs gastos  3) Sueldos + gastos por tipo  4) Detalle mensual
 */
@Service
class PdfBalanceService {

    private object Paleta {
        val tinta = Color(0x0F, 0x17, 0x2A)
        val gris = Color(0x64, 0x74, 0x8B)
        val grisClaro = Color(0xE2, 0xE8, 0xF0)
        val fondoTarjeta = Color(0xF1, 0xF5, 0xF9)
        val verde = Color(0x16, 0xA3, 0x4A)
        val verdeClaro = Color(0x4A, 0xDE, 0x80)
        val rojo = Color(0xDC, 0x26, 0x26)
        val rojoClaro = Color(0xFC, 0xA5, 0xA5)
        val azul = Color(0x25, 0x63, 0xEB)
        val azulClaro = Color(0x93, 0xC5, 0xFD)
    }

    private class Serie(
        val ars: List<Double>,
        val usd: List<Double?>,
        val color: Color,
        val colorClaro: Color
    )

    private val locale: Locale = Locale.forLanguageTag("es-AR")
    private val pagina = PageSize.A4.rotate()
    private val W = pagina.width
    private val H = pagina.height
    private val M = 40f
    private val totalPaginas = 4

    private val bf: BaseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED)
    private val bfBold: BaseFont = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED)

    private val meses = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    // ==================== ENTRADA ====================

    fun generarBalance(r: BalanceReporte): ByteArray {
        val document = Document(pagina, 0f, 0f, 0f, 0f)
        val baos = ByteArrayOutputStream()
        val writer = PdfWriter.getInstance(document, baos)
        document.open()
        val cb = writer.directContent
        val logo = cargarLogo(r.logoUrl)

        paginaResumen(cb, r, logo)
        pie(cb, r, 1)
        siguientePagina(document, writer)

        paginaIngresosGastos(cb, r, logo)
        pie(cb, r, 2)
        siguientePagina(document, writer)

        paginaSueldosYTipos(cb, r, logo)
        pie(cb, r, 3)
        siguientePagina(document, writer)

        paginaDetalle(cb, r, logo)
        pie(cb, r, 4)
        writer.setPageEmpty(false) // sin esto OpenPDF descarta lo dibujado en la última página
        document.close()
        return baos.toByteArray()
    }

    /** Lo dibujado en directContent no cuenta como "contenido", hay que forzarlo. */
    private fun siguientePagina(document: Document, writer: PdfWriter) {
        writer.setPageEmpty(false)
        document.newPage()
    }

    // ==================== PÁGINAS ====================

    private fun paginaResumen(cb: PdfContentByte, r: BalanceReporte, logo: Image?) {
        encabezado(cb, logo, "Balance", "${r.empresaNombre}  ·  ${periodo(r)}")

        val m = r.meses
        val ingresos = m.sumOf { it.ingresos }
        val gEvento = m.sumOf { it.gastosEvento }
        val gGenerales = m.sumOf { it.gastosGenerales }
        val sueldos = m.sumOf { it.sueldos }
        val resultado = ingresos - gEvento - gGenerales
        val eventos = m.sumOf { it.eventos }
        val personas = m.sumOf { it.personas }

        fun usdStr(total: Double?): String? = total?.let { usd(it) }

        val gap = 14f
        val cw = (W - 2 * M - 3 * gap) / 4f
        val h1 = 125f
        val h2 = 105f
        val y1 = H - 100f - 20f - h1
        val y2 = y1 - gap - h2

        // Fila 1: plata
        tarjeta(cb, M, y1, cw, h1, "Ingresos de eventos", ars(ingresos), Paleta.verde,
            usdStr(r.usdTotal { it.ingresos }), "Cobrado en el período")
        tarjeta(cb, M + (cw + gap), y1, cw, h1, "Gastos de eventos", ars(gEvento), Paleta.rojo,
            usdStr(r.usdTotal { it.gastosEvento }), "${porcentaje(gEvento, ingresos)} de los ingresos")
        tarjeta(cb, M + 2 * (cw + gap), y1, cw, h1, "Gastos generales", ars(gGenerales), Paleta.rojo,
            usdStr(r.usdTotal { it.gastosGenerales }), "Incluye sueldos ${ars(sueldos)}")
        tarjeta(cb, M + 3 * (cw + gap), y1, cw, h1, "Resultado neto",
            ars(resultado), if (resultado >= 0) Paleta.verde else Paleta.rojo,
            usdStr(r.usdTotal { it.resultado }), "Margen ${porcentaje(resultado, ingresos)}")

        // Fila 2: actividad
        val promedioEvento = if (eventos > 0) ingresos / eventos else 0.0
        val promedioEventoUsd = if (eventos > 0) r.usdTotal { it.ingresos }?.let { it / eventos } else null
        val mejor: BalanceMes? = m.maxByOrNull { it.resultado }

        tarjeta(cb, M, y2, cw, h2, "Eventos realizados", numero(eventos), Paleta.azul, null,
            "${decimal(eventos.toDouble() / m.size)} por mes")
        tarjeta(cb, M + (cw + gap), y2, cw, h2, "Personas (adultos + niños)", numero(personas), Paleta.azul, null,
            if (eventos > 0) "${decimal(personas.toDouble() / eventos)} por evento" else null)
        tarjeta(cb, M + 2 * (cw + gap), y2, cw, h2, "Ingreso promedio por evento", ars(promedioEvento), Paleta.verde,
            usdStr(promedioEventoUsd), null)
        tarjeta(cb, M + 3 * (cw + gap), y2, cw, h2, "Mejor mes",
            if (mejor != null) mesLargo(mejor.periodo) else "-", Paleta.tinta, null,
            if (mejor != null) "Resultado ${ars(mejor.resultado)}" else null)

        // Notas
        var ny = y2 - 34f
        txt(cb, "CÓMO LEER ESTE INFORME", M, ny, 8.5f, true, Paleta.gris)

        val notas = mutableListOf<String>()

        if (r.usdDisponible) {
            notas += "Los importes están en pesos. Debajo, el equivalente en dólares (dólar ${r.casaDolar} del día 1 de cada mes), para comparar sin el efecto de la inflación."

            val cotizacionesTexto = r.meses
                .filter { it.cotizacion != null }
                .joinToString(", ") { mes ->
                    "${mesCorto(mes.periodo)}: ${ars(mes.cotizacion!!)}"
                }

            if (cotizacionesTexto.isNotBlank()) {
                notas += "Cotizaciones utilizadas: $cotizacionesTexto"
            }
        } else {
            notas += "Los importes están en pesos. No se pudo obtener la cotización del dólar, por eso no se muestran equivalentes en USD."
        }

        notas += "Ingresos: pagos de eventos cobrados en el período. Gastos de eventos: gastos de tipo Evento. Gastos generales: el resto de los gastos."
        notas += "Eventos y personas se cuentan por fecha del evento. Los ingresos, por fecha de cobro."

        val anchoDisponible = W - 2 * M

        ny -= 12f
        notas.forEach { nota ->
            ny = txtMultilinea(cb, nota, M, ny, anchoDisponible, 8.5f, false, Paleta.gris)
        }
    }

    private fun paginaIngresosGastos(cb: PdfContentByte, r: BalanceReporte, logo: Image?) {
        encabezado(
            cb, logo, "Ingresos y gastos por mes",
            if (r.usdDisponible) "Barras oscuras: pesos (eje izquierdo)  ·  Barras claras: dólares (eje derecho)"
            else "Valores en pesos"
        )
        val m = r.meses
        val series = listOf(
            Serie(m.map { it.ingresos }, m.map { x -> x.usd(x.ingresos) }, Paleta.verde, Paleta.verdeClaro),
            Serie(m.map { it.gastos }, m.map { x -> x.usd(x.gastos) }, Paleta.rojo, Paleta.rojoClaro)
        )
        val items = mutableListOf("Ingresos" to Paleta.verde)
        if (r.usdDisponible) items += "Ingresos en USD" to Paleta.verdeClaro
        items += "Gastos" to Paleta.rojo
        if (r.usdDisponible) items += "Gastos en USD" to Paleta.rojoClaro
        leyenda(cb, M, H - 106f, items)

        graficoBarras(cb, M, 56f, W - 2 * M, H - 125f - 56f, m.map { mesCorto(it.periodo) }, series, r.usdDisponible)
    }

    private fun paginaSueldosYTipos(cb: PdfContentByte, r: BalanceReporte, logo: Image?) {
        encabezado(cb, logo, "Sueldos y distribución de gastos", periodo(r))
        val m = r.meses

        // Izquierda: sueldos por mes
        val leftW = 480f
        txt(cb, "Sueldos por mes", M, H - 106f, 12f, true)
        val items = mutableListOf("Sueldos" to Paleta.azul)
        if (r.usdDisponible) items += "Sueldos en USD" to Paleta.azulClaro
        leyenda(cb, M + 130f, H - 106f, items)
        val serie = Serie(m.map { it.sueldos }, m.map { x -> x.usd(x.sueldos) }, Paleta.azul, Paleta.azulClaro)
        graficoBarras(cb, M, 56f, leftW, H - 125f - 56f, m.map { mesCorto(it.periodo) }, listOf(serie), r.usdDisponible)

        // Derecha: gastos por tipo
        val rx = M + leftW + 40f
        txt(cb, "Gastos por tipo", rx, H - 106f, 12f, true)
        val datos = r.gastosPorTipo.entries
            .filter { it.value > 0.0 }
            .sortedByDescending { it.value }
            .map { nombreTipo(it.key) to it.value }
        gastosPorTipo(cb, rx, H - 125f, W - M - rx, datos)
    }

    private fun paginaDetalle(cb: PdfContentByte, r: BalanceReporte, logo: Image?) {
        encabezado(
            cb, logo, "Detalle mensual",
            if (r.usdDisponible) "Valores en pesos  ·  Dólar ${r.casaDolar} del día 1 de cada mes"
            else "Valores en pesos"
        )
        val m = r.meses
        val cols = listOf(
            "Mes" to 100f, "Ingresos" to 100f, "Gastos de evento" to 100f, "Gastos generales" to 100f,
            "Sueldos" to 90f, "Resultado" to 100f, "Eventos" to 50f, "Personas" to 62f, "Dólar día 1" to 60f
        )
        val xs = cols.runningFold(M) { acc, c -> acc + c.second }

        val top = H - 100f
        val rowH = min(24f, (top - 56f) / (m.size + 3))
        val fs = when {
            rowH >= 20f -> 9.5f
            rowH >= 16f -> 9f
            else -> 8f
        }

        fun fila(yBase: Float, valores: List<String>, bold: Boolean, colorResultado: Color? = null, colorBase: Color = Paleta.tinta) {
            val ty = yBase + rowH / 2f - fs / 3f
            valores.forEachIndexed { i, v ->
                val color = if (i == 5 && colorResultado != null) colorResultado else colorBase
                if (i == 0) txt(cb, v, xs[0] + 10f, ty, fs, bold, color)
                else txt(cb, v, xs[i] + cols[i].second - 8f, ty, fs, bold, color, Element.ALIGN_RIGHT)
            }
        }

        // Encabezado de tabla
        var cy = top - rowH
        rect(cb, M, cy, W - 2 * M, rowH, Paleta.tinta, 6f)
        cols.forEachIndexed { i, c ->
            val ty = cy + rowH / 2f - 3f
            if (i == 0) txt(cb, c.first, xs[0] + 10f, ty, 8f, true, Color.WHITE)
            else txt(cb, c.first, xs[i] + c.second - 8f, ty, 8f, true, Color.WHITE, Element.ALIGN_RIGHT)
        }

        // Filas
        m.forEachIndexed { idx, mes ->
            cy -= rowH
            if (idx % 2 == 0) rect(cb, M, cy, W - 2 * M, rowH, Paleta.fondoTarjeta)
            fila(
                cy,
                listOf(
                    mesLargo(mes.periodo), ars(mes.ingresos), ars(mes.gastosEvento), ars(mes.gastosGenerales),
                    ars(mes.sueldos), ars(mes.resultado), numero(mes.eventos), numero(mes.personas),
                    mes.cotizacion?.let { ars(it) } ?: "-"
                ),
                false,
                if (mes.resultado >= 0) Paleta.verde else Paleta.rojo
            )
        }

        // Totales
        val ing = m.sumOf { it.ingresos }
        val gev = m.sumOf { it.gastosEvento }
        val gge = m.sumOf { it.gastosGenerales }
        val sue = m.sumOf { it.sueldos }
        val res = ing - gev - gge
        cy -= rowH
        rect(cb, M, cy, W - 2 * M, rowH, Paleta.grisClaro, 6f)
        fila(
            cy,
            listOf("TOTAL", ars(ing), ars(gev), ars(gge), ars(sue), ars(res),
                numero(m.sumOf { it.eventos }), numero(m.sumOf { it.personas }), ""),
            true,
            if (res >= 0) Paleta.verde else Paleta.rojo
        )

        // Total en USD (suma de cada mes convertido a su propia cotización)
        if (r.usdDisponible) {
            cy -= rowH
            fila(
                cy,
                listOf(
                    "Total en USD",
                    usd(r.usdTotal { it.ingresos } ?: 0.0),
                    usd(r.usdTotal { it.gastosEvento } ?: 0.0),
                    usd(r.usdTotal { it.gastosGenerales } ?: 0.0),
                    usd(r.usdTotal { it.sueldos } ?: 0.0),
                    usd(r.usdTotal { it.resultado } ?: 0.0),
                    "", "", ""
                ),
                false,
                null,
                Paleta.gris
            )
        }
    }

    // ==================== COMPONENTES ====================

    private fun encabezado(cb: PdfContentByte, logo: Image?, titulo: String, subtitulo: String) {
        rect(cb, 0f, H - 8f, W, 8f, Paleta.tinta)
        txt(cb, titulo, M, H - 52f, 24f, true)
        txt(cb, subtitulo, M, H - 72f, 11f, false, Paleta.gris)
        if (logo != null) {
            logo.setAbsolutePosition(W - M - logo.scaledWidth, H - 24f - logo.scaledHeight)
            cb.addImage(logo)
        }
    }

    private fun pie(cb: PdfContentByte, r: BalanceReporte, numero: Int) {
        linea(cb, M, 40f, W - M, 40f, Paleta.grisClaro, 0.8f)
        txt(cb, "${r.empresaNombre}  ·  Balance ${periodo(r)}", M, 26f, 8f, false, Paleta.gris)
        val hoy = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        txt(cb, "Generado el $hoy  ·  Página $numero de $totalPaginas", W - M, 26f, 8f, false, Paleta.gris, Element.ALIGN_RIGHT)
    }

    private fun tarjeta(
        cb: PdfContentByte, x: Float, y: Float, w: Float, h: Float,
        etiqueta: String, valor: String, color: Color, usd: String?, detalle: String?
    ) {
        rect(cb, x, y, w, h, Paleta.fondoTarjeta, 10f)
        rect(cb, x, y + 14f, 4f, h - 28f, color)
        txt(cb, etiqueta.uppercase(locale), x + 18f, y + h - 26f, 8.5f, true, Paleta.gris)

        var size = 22f
        while (bfBold.getWidthPoint(valor, size) > w - 32f && size > 12f) size -= 1f
        txt(cb, valor, x + 18f, y + h - 58f, size, true, color)

        if (usd != null) txt(cb, usd, x + 18f, y + h - 78f, 10f, false, Paleta.gris)
        if (detalle != null) txt(cb, detalle, x + 18f, y + 16f, 9f, false, Paleta.gris)
    }

    private fun leyenda(cb: PdfContentByte, x: Float, y: Float, items: List<Pair<String, Color>>) {
        var cx = x
        items.forEach { (nombre, color) ->
            rect(cb, cx, y, 9f, 9f, color, 2f)
            txt(cb, nombre, cx + 14f, y + 1f, 9f, false, Paleta.gris)
            cx += 14f + bf.getWidthPoint(nombre, 9f) + 18f
        }
    }

    /**
     * Barras agrupadas por mes. Cada serie se dibuja en pesos (barra ancha, eje izquierdo)
     * y, si hay dólares, una barra más angosta y clara encima (eje derecho).
     */
    private fun graficoBarras(
        cb: PdfContentByte, x: Float, y: Float, w: Float, h: Float,
        etiquetas: List<String>, series: List<Serie>, conUsd: Boolean
    ) {
        val ejeIzq = 56f
        val ejeDer = if (conUsd) 56f else 12f
        val plotX = x + ejeIzq
        val plotW = w - ejeIzq - ejeDer
        val plotY = y + 24f
        val plotH = h - 24f - 16f

        val maxArs = maximoLindo(series.maxOf { s -> s.ars.maxOrNull() ?: 0.0 })
        val maxUsd = if (conUsd) maximoLindo(series.maxOf { s -> s.usd.maxOf { it ?: 0.0 } }) else 1.0

        // Grilla y ejes
        for (i in 0..4) {
            val gy = plotY + plotH * i / 4f
            linea(cb, plotX, gy, plotX + plotW, gy, if (i == 0) Paleta.gris else Paleta.grisClaro, if (i == 0) 1f else 0.6f)
            txt(cb, compacto(maxArs * i / 4.0), plotX - 6f, gy - 3f, 8f, false, Paleta.gris, Element.ALIGN_RIGHT)
            if (conUsd) txt(cb, compacto(maxUsd * i / 4.0), plotX + plotW + 6f, gy - 3f, 8f, false, Paleta.gris, Element.ALIGN_LEFT)
        }
        txt(cb, "ARS", plotX - 6f, plotY + plotH + 6f, 8f, true, Paleta.gris, Element.ALIGN_RIGHT)
        if (conUsd) txt(cb, "USD", plotX + plotW + 6f, plotY + plotH + 6f, 8f, true, Paleta.gris, Element.ALIGN_LEFT)

        // Barras
        val n = etiquetas.size
        val grupoW = plotW / n

    // 1. Ajustamos el ancho del conjunto de barras del mes (de 0.72f a 0.80f para aprovechar mejor el espacio)
        val clusterW = grupoW * 0.85f
        val numSeries = series.size
        val barW = clusterW / numSeries

    // 2. Definimos un gap horizontal interno entre barras de un mismo grupo (ej: 3f)
        val gapInterno = if (numSeries > 1) 6f else 0f
        val anchoBarraEfectivo = barW - gapInterno

        for (i in 0 until n) {
            val gx = plotX + grupoW * i + (grupoW - clusterW) / 2f
            series.forEachIndexed { j, s ->
                // Posicionamos cada barra sumando el gap interno
                val bx = gx + barW * j + (gapInterno / 2f)
                val bh = (s.ars[i] / maxArs * plotH).toFloat()

                // Dibujamos la barra con el ancho ajustado
                rect(cb, bx, plotY, anchoBarraEfectivo, bh, s.color)

                var uh = 0f
                val u = s.usd[i]
                if (conUsd && u != null) {
                    uh = (u / maxUsd * plotH).toFloat()
                    val uw = anchoBarraEfectivo * 0.5f
                    rect(cb, bx + (anchoBarraEfectivo - uw) / 2f, plotY, uw, uh, s.colorClaro)
                }

                if (n <= 12 && s.ars[i] > 0.0) {
                    val centroBarra = bx + anchoBarraEfectivo / 2f
                    val yEtiquetaArs = plotY + max(bh, uh) + 3f

                    txt(cb, compacto(s.ars[i]), centroBarra, yEtiquetaArs, 7f, true, s.color, Element.ALIGN_CENTER)

                    if (conUsd) {
                        if (u != null && u > 0.0) {
                            val usdTexto = "${compacto(u)} USD"
                            txt(cb, usdTexto, centroBarra, yEtiquetaArs + 8f, 6.5f, false, Paleta.gris, Element.ALIGN_CENTER)
                        }
                    }
                }
            }
            txt(
                cb, etiquetas[i], plotX + grupoW * i + grupoW / 2f, plotY - 14f,
                if (n > 16) 7f else 8.5f, false, Paleta.gris, Element.ALIGN_CENTER
            )
        }
    }

    private fun gastosPorTipo(cb: PdfContentByte, x: Float, yTop: Float, w: Float, datos: List<Pair<String, Double>>) {
        val total = datos.sumOf { it.second }
        if (datos.isEmpty() || total <= 0.0) {
            txt(cb, "Sin gastos en el período", x, yTop - 20f, 10f, false, Paleta.gris)
            return
        }
        val rowH = min(40f, 380f / datos.size)
        val labelW = 96f
        val valW = 76f
        val barMax = w - labelW - valW
        val maxV = datos.maxOf { it.second }

        datos.forEachIndexed { i, (nombre, v) ->
            val ry = yTop - rowH * (i + 1)
            val centro = ry + rowH / 2f
            txt(cb, nombre, x, centro - 3f, 9.5f, false, Paleta.tinta)
            val bw = (v / maxV * barMax).toFloat().coerceAtLeast(2f)
            rect(cb, x + labelW, centro - 7f, bw, 14f, Paleta.rojo, 3f)
            txt(cb, "$${compacto(v)}  ${porcentaje(v, total)}", x + labelW + bw + 6f, centro - 3f, 8.5f, true, Paleta.gris)
        }
    }

    // ==================== PRIMITIVAS ====================

    private fun txt(
        cb: PdfContentByte, s: String, x: Float, y: Float, size: Float,
        bold: Boolean = false, color: Color = Paleta.tinta, align: Int = Element.ALIGN_LEFT
    ) {
        cb.beginText()
        cb.setFontAndSize(if (bold) bfBold else bf, size)
        cb.setColorFill(color)
        cb.showTextAligned(align, s, x, y, 0f)
        cb.endText()
    }

    private fun rect(cb: PdfContentByte, x: Float, y: Float, w: Float, h: Float, color: Color, radio: Float = 0f) {
        if (w <= 0f || h <= 0f) return
        cb.setColorFill(color)
        if (radio > 0f) cb.roundRectangle(x, y, w, h, min(radio, min(w, h) / 2f)) else cb.rectangle(x, y, w, h)
        cb.fill()
    }

    private fun linea(cb: PdfContentByte, x1: Float, y1: Float, x2: Float, y2: Float, color: Color, ancho: Float) {
        cb.setColorStroke(color)
        cb.setLineWidth(ancho)
        cb.moveTo(x1, y1)
        cb.lineTo(x2, y2)
        cb.stroke()
    }

    private fun cargarLogo(url: String?): Image? {
        if (url.isNullOrBlank()) return null
        return try {
            Image.getInstance(URL(url)).apply { scaleToFit(130f, 50f) }
        } catch (e: Exception) {
            null // sin logo el informe sigue funcionando
        }
    }

    // ==================== FORMATO ====================

    private fun ars(v: Double): String {
        val n = Math.round(abs(v))
        val signo = if (v < 0 && n != 0L) "-" else ""
        return signo + "\$" + NumberFormat.getIntegerInstance(locale).format(n)
    }

    private fun usd(v: Double): String {
        val n = Math.round(abs(v))
        val signo = if (v < 0 && n != 0L) "-" else ""
        return signo + "US\$ " + NumberFormat.getIntegerInstance(locale).format(n)
    }

    private fun numero(v: Int): String = NumberFormat.getIntegerInstance(locale).format(v)

    private fun decimal(v: Double): String = String.format(locale, "%.1f", v).removeSuffix(",0")

    private fun porcentaje(parte: Double, total: Double): String =
        if (total == 0.0) "0%" else String.format(locale, "%.1f", parte / total * 100.0).removeSuffix(",0") + "%"

    /** 7500000 -> "7,5M", 12000 -> "12K". */
    private fun compacto(v: Double): String {
        fun fmt(x: Double, sufijo: String) = String.format(locale, "%.1f", x).removeSuffix(",0") + sufijo
        val a = abs(v)
        return when {
            a >= 1_000_000 -> fmt(v / 1_000_000, "M")
            a >= 1_000 -> fmt(v / 1_000, "K")
            else -> String.format(locale, "%.0f", v)
        }
    }

    private fun maximoLindo(v: Double): Double {
        if (v <= 0.0) return 1.0
        val base = Math.pow(10.0, Math.floor(Math.log10(v)))
        val f = v / base
        val lindo = listOf(1.0, 1.2, 1.5, 2.0, 2.5, 3.0, 4.0, 5.0, 6.0, 8.0, 10.0).first { it >= f - 1e-9 }
        return lindo * base
    }

    private fun mesLargo(p: YearMonth) = "${meses[p.monthValue - 1]} ${p.year}"
    private fun mesCorto(p: YearMonth) = "${meses[p.monthValue - 1].take(3)} ${(p.year % 100).toString().padStart(2, '0')}"
    private fun periodo(r: BalanceReporte) =
        if (r.desde == r.hasta) mesLargo(r.desde) else "${mesLargo(r.desde)} – ${mesLargo(r.hasta)}"

    private fun nombreTipo(t: TipoGasto): String = when (t) {
        TipoGasto.EVENTO -> "Eventos"
        TipoGasto.MANTENIMIENTO -> "Mantenimiento"
        TipoGasto.MERCADERIA -> "Mercadería"
        TipoGasto.SUELDOS -> "Sueldos"
        TipoGasto.IMPUESTOS -> "Impuestos"
        TipoGasto.SERVICIOS -> "Servicios"
        TipoGasto.OTROS -> "Otros"
        TipoGasto.TARJETA_CREDITO -> "Tarjeta de credito"
    }

    private fun txtMultilinea(
        cb: PdfContentByte, s: String, x: Float, ySuperior: Float, anchoMax: Float,
        fontSize: Float, bold: Boolean = false, color: Color = Paleta.gris
    ): Float {
        val ct = ColumnText(cb)
        val font = Font(if (bold) bfBold else bf, fontSize, Font.NORMAL, color)
        ct.setSimpleColumn(x, 0f, x + anchoMax, ySuperior)
        ct.addText(Phrase(s, font))
        ct.go()
        return ct.yLine // Retorna la posición Y resultante tras dibujar las líneas
    }
}