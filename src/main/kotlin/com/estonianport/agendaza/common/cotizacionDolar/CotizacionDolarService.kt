package com.estonianport.agendaza.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.util.TreeMap

/**
 * Trae la serie histórica del dólar desde argentinadatos.com y devuelve la cotización
 * (venta) del día 1 de cada mes. Si ese día no hay dato (feriado / fin de semana),
 * usa el último valor disponible anterior.
 *
 * La "casa" se configura en application.properties: balance.dolar.casa=blue
 * (valores posibles: oficial, blue, bolsa, contadoconliqui, mayorista, etc.)
 */
@Service
class CotizacionDolarService(
    private val objectMapper: ObjectMapper,
    @Value("\${balance.dolar.casa:blue}") val casa: String
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val client: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build()

    @Volatile private var cache: TreeMap<LocalDate, Double>? = null
    @Volatile private var cacheTs: Instant = Instant.EPOCH

    fun primerDiaDeCadaMes(periodos: List<YearMonth>): Map<YearMonth, Double?> {
        val tabla = obtenerTabla()
        return periodos.associateWith { ym -> tabla?.floorEntry(ym.atDay(1))?.value }
    }

    private fun obtenerTabla(): TreeMap<LocalDate, Double>? {
        val actual = cache
        if (actual != null && Duration.between(cacheTs, Instant.now()) < TTL) return actual

        return try {
            val request = HttpRequest.newBuilder(
                URI.create("https://api.argentinadatos.com/v1/cotizaciones/dolares/$casa")
            ).timeout(Duration.ofSeconds(15)).GET().build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            check(response.statusCode() == 200) { "HTTP ${response.statusCode()}" }

            val tabla = TreeMap<LocalDate, Double>()
            objectMapper.readTree(response.body()).forEach { nodo ->
                val venta = nodo.get("venta")?.asDouble() ?: 0.0
                val fecha = nodo.get("fecha")?.asText()
                if (venta > 0.0 && fecha != null) tabla[LocalDate.parse(fecha)] = venta
            }
            check(tabla.isNotEmpty()) { "respuesta vacía" }

            cache = tabla
            cacheTs = Instant.now()
            tabla
        } catch (e: Exception) {
            log.warn("No se pudo obtener la cotización del dólar ($casa): ${e.message}")
            actual // si había una copia vieja, mejor eso que nada
        }
    }

    companion object {
        private val TTL: Duration = Duration.ofHours(6)
    }
}