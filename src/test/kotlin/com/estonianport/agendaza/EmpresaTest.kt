package com.estonianport.agendaza

import com.estonianport.agendaza.model.EventoExtraVariable
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.PrecioConFechaExtra
import com.estonianport.agendaza.model.PrecioConFechaTipoEvento
import com.estonianport.agendaza.model.Prestador
import com.estonianport.agendaza.model.Salon
import com.estonianport.agendaza.model.TipoEvento
import com.estonianport.agendaza.model.enums.TipoPrestador
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Testea la lógica de búsqueda de precios por fecha en Empresa.
 * Usamos la subclase concreta Salon para instanciar (Empresa es abstract).
 */
class EmpresaTest {

    private lateinit var empresa: Salon
    private lateinit var extra: Extra
    private lateinit var tipoEvento: TipoEvento

    private val fecha = LocalDateTime.of(2025, 8, 20, 18, 0)

    @BeforeEach
    fun setUp() {
        empresa    = Salon(1L, "Salón Test", 1234567890L, "test@test.com", "Calle", 123, "Ciudad")
        extra      = mock<Extra>().also { whenever(it.id).thenReturn(10L) }
        tipoEvento = mock<TipoEvento>().also { whenever(it.id).thenReturn(20L) }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun pricioExtra(
        extraId: Long = 10L,
        precio: Double,
        desde: LocalDateTime,
        hasta: LocalDateTime,
        fechaBaja: LocalDate? = null
    ): PrecioConFechaExtra {
        val extMock = mock<Extra>().also { whenever(it.id).thenReturn(extraId) }
        return mock<PrecioConFechaExtra>().also {
            whenever(it.extra).thenReturn(extMock)
            whenever(it.precio).thenReturn(precio)
            whenever(it.desde).thenReturn(desde)
            whenever(it.hasta).thenReturn(hasta)
            whenever(it.fechaBaja).thenReturn(fechaBaja)
        }
    }

    // ── getPrecioOfExtraByFecha ───────────────────────────────────────────────

    @Nested
    inner class GetPrecioOfExtraByFechaTest {

        @Test
        fun `devuelve precio cuando fecha esta dentro del rango`() {
            val precio = pricioExtra(
                precio = 300.0,
                desde  = fecha.minusDays(10),
                hasta  = fecha.plusDays(10)
            )
            empresa.listaPrecioConFechaExtra = mutableSetOf(precio)

            assertEquals(300.0, empresa.getPrecioOfExtraByFecha(extra, fecha))
        }

        @Test
        fun `devuelve 0 cuando no hay precio vigente para la fecha`() {
            val precio = pricioExtra(
                precio = 300.0,
                desde  = fecha.plusDays(1),
                hasta  = fecha.plusDays(10)
            )
            empresa.listaPrecioConFechaExtra = mutableSetOf(precio)

            assertEquals(0.0, empresa.getPrecioOfExtraByFecha(extra, fecha))
        }

        @Test
        fun `devuelve 0 cuando el precio esta dado de baja`() {
            val precio = pricioExtra(
                precio   = 300.0,
                desde    = fecha.minusDays(10),
                hasta    = fecha.plusDays(10),
                fechaBaja = LocalDate.now()
            )
            empresa.listaPrecioConFechaExtra = mutableSetOf(precio)

            assertEquals(0.0, empresa.getPrecioOfExtraByFecha(extra, fecha))
        }

        @Test
        fun `devuelve 0 cuando la lista esta vacia`() {
            empresa.listaPrecioConFechaExtra = mutableSetOf()
            assertEquals(0.0, empresa.getPrecioOfExtraByFecha(extra, fecha))
        }

        @Test
        fun `devuelve el precio del extra correcto cuando hay varios extras`() {
            val precioExtra10 = pricioExtra(extraId = 10L, precio = 200.0,
                desde = fecha.minusDays(5), hasta = fecha.plusDays(5))
            val precioExtra99 = pricioExtra(extraId = 99L, precio = 999.0,
                desde = fecha.minusDays(5), hasta = fecha.plusDays(5))
            empresa.listaPrecioConFechaExtra = mutableSetOf(precioExtra10, precioExtra99)

            assertEquals(200.0, empresa.getPrecioOfExtraByFecha(extra, fecha))
        }
    }

    // ── getPrecioOfExtraVariableByFecha ───────────────────────────────────────

    @Nested
    inner class GetPrecioOfExtraVariableByFechaTest {

        @Test
        fun `multiplica precio unitario por cantidad`() {
            val precio = pricioExtra(
                precio = 100.0,
                desde  = fecha.minusDays(1),
                hasta  = fecha.plusDays(1)
            )
            empresa.listaPrecioConFechaExtra = mutableSetOf(precio)

            val extraVariable = EventoExtraVariable(1L, extra, 3) // cantidad 3

            assertEquals(300.0, empresa.getPrecioOfExtraVariableByFecha(extraVariable, fecha))
        }

        @Test
        fun `devuelve 0 si el extra variable no tiene precio en la fecha`() {
            empresa.listaPrecioConFechaExtra = mutableSetOf()
            val extraVariable = EventoExtraVariable(1L, extra, 5)
            assertEquals(0.0, empresa.getPrecioOfExtraVariableByFecha(extraVariable, fecha))
        }
    }

    // ── getSumOfPrecioByListaExtra ────────────────────────────────────────────

    @Nested
    inner class GetSumOfPrecioByListaExtraTest {

        @Test
        fun `suma correctamente los precios de una lista de extras`() {
            val extra2 = mock<Extra>().also { whenever(it.id).thenReturn(11L) }

            val precio1 = pricioExtra(extraId = 10L, precio = 500.0,
                desde = fecha.minusDays(1), hasta = fecha.plusDays(1))
            val precio2 = pricioExtra(extraId = 11L, precio = 250.0,
                desde = fecha.minusDays(1), hasta = fecha.plusDays(1))
            empresa.listaPrecioConFechaExtra = mutableSetOf(precio1, precio2)

            val suma = empresa.getSumOfPrecioByListaExtra(listOf(extra, extra2), fecha)
            assertEquals(750.0, suma, 0.001)
        }

        @Test
        fun `devuelve 0 con lista vacia`() {
            assertEquals(0.0, empresa.getSumOfPrecioByListaExtra(emptyList(), fecha))
        }
    }

    // ── copy ─────────────────────────────────────────────────────────────────

    @Nested
    inner class CopyTest {

        @Test
        fun `copy de Salon devuelve nuevo Salon con datos actualizados`() {
            val copia = empresa.copy("Nuevo Nombre", 9876543210L, "nuevo@e.com", "Av", 999, "OtraCiudad")
            assertInstanceOf(Salon::class.java, copia)
            assertEquals("Nuevo Nombre", copia.nombre)
            assertEquals(empresa.id, copia.id)   // mantiene el mismo ID
        }

        @Test
        fun `copy de Prestador devuelve nuevo Prestador`() {
            val prestador = Prestador(2L, "P", 111L, "p@p.com", "C", 1, "M", TipoPrestador.DJ)
            val copia = prestador.copy("DJs SRL", 222L, "djs@djs.com", "Ruta", 0, "Capital")
            assertInstanceOf(Prestador::class.java, copia)
            assertEquals("DJs SRL", copia.nombre)
        }
    }
}