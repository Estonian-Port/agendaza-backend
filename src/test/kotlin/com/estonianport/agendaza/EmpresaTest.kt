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

    private fun precioExtra(
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

    private fun precioTipoEvento(
        tipoEventoId: Long = 20L,
        precio: Double,
        desde: LocalDateTime,
        hasta: LocalDateTime,
        fechaBaja: LocalDate? = null
    ): PrecioConFechaTipoEvento {
        val teMock = mock<TipoEvento>().also { whenever(it.id).thenReturn(tipoEventoId) }
        return mock<PrecioConFechaTipoEvento>().also {
            whenever(it.tipoEvento).thenReturn(teMock)
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
            empresa.listaPrecioConFechaExtra = mutableSetOf(
                precioExtra(precio = 300.0, desde = fecha.minusDays(10), hasta = fecha.plusDays(10))
            )
            assertEquals(300.0, empresa.getPrecioOfExtraByFecha(extra, fecha))
        }

        @Test
        fun `devuelve 0 cuando no hay precio vigente para la fecha`() {
            empresa.listaPrecioConFechaExtra = mutableSetOf(
                precioExtra(precio = 300.0, desde = fecha.plusDays(1), hasta = fecha.plusDays(10))
            )
            assertEquals(0.0, empresa.getPrecioOfExtraByFecha(extra, fecha))
        }

        @Test
        fun `devuelve 0 cuando el precio esta dado de baja`() {
            empresa.listaPrecioConFechaExtra = mutableSetOf(
                precioExtra(precio = 300.0, desde = fecha.minusDays(10), hasta = fecha.plusDays(10),
                    fechaBaja = LocalDate.now())
            )
            assertEquals(0.0, empresa.getPrecioOfExtraByFecha(extra, fecha))
        }

        @Test
        fun `devuelve 0 cuando la lista esta vacia`() {
            empresa.listaPrecioConFechaExtra = mutableSetOf()
            assertEquals(0.0, empresa.getPrecioOfExtraByFecha(extra, fecha))
        }

        @Test
        fun `devuelve el precio del extra correcto cuando hay varios extras`() {
            empresa.listaPrecioConFechaExtra = mutableSetOf(
                precioExtra(extraId = 10L, precio = 200.0, desde = fecha.minusDays(5), hasta = fecha.plusDays(5)),
                precioExtra(extraId = 99L, precio = 999.0, desde = fecha.minusDays(5), hasta = fecha.plusDays(5))
            )
            assertEquals(200.0, empresa.getPrecioOfExtraByFecha(extra, fecha))
        }

        @Test
        fun `devuelve 0 si el extra no pertenece a esta empresa`() {
            val otroExtra = mock<Extra>().also { whenever(it.id).thenReturn(999L) }
            empresa.listaPrecioConFechaExtra = mutableSetOf(
                precioExtra(extraId = 10L, precio = 500.0, desde = fecha.minusDays(1), hasta = fecha.plusDays(1))
            )
            assertEquals(0.0, empresa.getPrecioOfExtraByFecha(otroExtra, fecha))
        }
    }

    // ── getPrecioOfTipoEvento ─────────────────────────────────────────────────

    @Nested
    inner class GetPrecioOfTipoEventoTest {

        @Test
        fun `devuelve precio cuando fecha esta dentro del rango`() {
            empresa.listaPrecioConFechaTipoEvento = mutableSetOf(
                precioTipoEvento(precio = 5000.0, desde = fecha.minusDays(5), hasta = fecha.plusDays(5))
            )
            assertEquals(5000.0, empresa.getPrecioOfTipoEvento(20L, fecha))
        }

        @Test
        fun `devuelve 0 cuando la lista esta vacia`() {
            empresa.listaPrecioConFechaTipoEvento = mutableSetOf()
            assertEquals(0.0, empresa.getPrecioOfTipoEvento(20L, fecha))
        }

        @Test
        fun `devuelve 0 cuando el tipo evento esta dado de baja`() {
            empresa.listaPrecioConFechaTipoEvento = mutableSetOf(
                precioTipoEvento(precio = 5000.0, desde = fecha.minusDays(5), hasta = fecha.plusDays(5),
                    fechaBaja = LocalDate.now())
            )
            assertEquals(0.0, empresa.getPrecioOfTipoEvento(20L, fecha))
        }

        @Test
        fun `devuelve 0 cuando la fecha esta fuera del rango`() {
            empresa.listaPrecioConFechaTipoEvento = mutableSetOf(
                precioTipoEvento(precio = 5000.0, desde = fecha.plusDays(1), hasta = fecha.plusDays(10))
            )
            assertEquals(0.0, empresa.getPrecioOfTipoEvento(20L, fecha))
        }

        @Test
        fun `devuelve el precio del tipo evento correcto entre varios`() {
            empresa.listaPrecioConFechaTipoEvento = mutableSetOf(
                precioTipoEvento(tipoEventoId = 20L, precio = 8000.0,
                    desde = fecha.minusDays(1), hasta = fecha.plusDays(1)),
                precioTipoEvento(tipoEventoId = 99L, precio = 1000.0,
                    desde = fecha.minusDays(1), hasta = fecha.plusDays(1))
            )
            assertEquals(8000.0, empresa.getPrecioOfTipoEvento(20L, fecha))
        }
    }

    // ── getPrecioOfExtraVariableByFecha ───────────────────────────────────────

    @Nested
    inner class GetPrecioOfExtraVariableByFechaTest {

        @Test
        fun `multiplica precio unitario por cantidad`() {
            empresa.listaPrecioConFechaExtra = mutableSetOf(
                precioExtra(precio = 100.0, desde = fecha.minusDays(1), hasta = fecha.plusDays(1))
            )
            val extraVariable = EventoExtraVariable(1L, extra, 3)
            assertEquals(300.0, empresa.getPrecioOfExtraVariableByFecha(extraVariable, fecha))
        }

        @Test
        fun `devuelve 0 si el extra variable no tiene precio en la fecha`() {
            empresa.listaPrecioConFechaExtra = mutableSetOf()
            val extraVariable = EventoExtraVariable(1L, extra, 5)
            assertEquals(0.0, empresa.getPrecioOfExtraVariableByFecha(extraVariable, fecha))
        }

        @Test
        fun `con cantidad 0 devuelve 0 aunque haya precio`() {
            empresa.listaPrecioConFechaExtra = mutableSetOf(
                precioExtra(precio = 500.0, desde = fecha.minusDays(1), hasta = fecha.plusDays(1))
            )
            val extraVariable = EventoExtraVariable(1L, extra, 0)
            assertEquals(0.0, empresa.getPrecioOfExtraVariableByFecha(extraVariable, fecha))
        }
    }

    // ── getSumOfPrecioByListaExtra ────────────────────────────────────────────

    @Nested
    inner class GetSumOfPrecioByListaExtraTest {

        @Test
        fun `suma correctamente los precios de una lista de extras`() {
            val extra2 = mock<Extra>().also { whenever(it.id).thenReturn(11L) }

            empresa.listaPrecioConFechaExtra = mutableSetOf(
                precioExtra(extraId = 10L, precio = 500.0, desde = fecha.minusDays(1), hasta = fecha.plusDays(1)),
                precioExtra(extraId = 11L, precio = 250.0, desde = fecha.minusDays(1), hasta = fecha.plusDays(1))
            )

            assertEquals(750.0, empresa.getSumOfPrecioByListaExtra(listOf(extra, extra2), fecha), 0.001)
        }

        @Test
        fun `devuelve 0 con lista vacia`() {
            assertEquals(0.0, empresa.getSumOfPrecioByListaExtra(emptyList(), fecha))
        }

        @Test
        fun `ignora extras sin precio vigente`() {
            val extra2 = mock<Extra>().also { whenever(it.id).thenReturn(11L) }
            empresa.listaPrecioConFechaExtra = mutableSetOf(
                precioExtra(extraId = 10L, precio = 400.0, desde = fecha.minusDays(1), hasta = fecha.plusDays(1))
                // extra2 (id=11) no tiene precio configurado
            )
            assertEquals(400.0, empresa.getSumOfPrecioByListaExtra(listOf(extra, extra2), fecha), 0.001)
        }
    }

    // ── getSumOfPrecioByListaExtraVariable ────────────────────────────────────

    @Nested
    inner class GetSumOfPrecioByListaExtraVariableTest {

        @Test
        fun `suma correctamente multiples extras variables`() {
            val extra2 = mock<Extra>().also { whenever(it.id).thenReturn(11L) }

            empresa.listaPrecioConFechaExtra = mutableSetOf(
                precioExtra(extraId = 10L, precio = 100.0, desde = fecha.minusDays(1), hasta = fecha.plusDays(1)),
                precioExtra(extraId = 11L, precio = 200.0, desde = fecha.minusDays(1), hasta = fecha.plusDays(1))
            )

            val var1 = EventoExtraVariable(1L, extra,  2)  // 2 * 100 = 200
            val var2 = EventoExtraVariable(2L, extra2, 3)  // 3 * 200 = 600

            assertEquals(800.0, empresa.getSumOfPrecioByListaExtraVariable(listOf(var1, var2), fecha), 0.001)
        }

        @Test
        fun `devuelve 0 con lista vacia`() {
            assertEquals(0.0, empresa.getSumOfPrecioByListaExtraVariable(emptyList(), fecha))
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
            assertEquals(empresa.id, copia.id)
        }

        @Test
        fun `copy de Prestador devuelve nuevo Prestador conservando tipoPrestador`() {
            val prestador = Prestador(2L, "P", 111L, "p@p.com", "C", 1, "M", TipoPrestador.DJ)
            val copia = prestador.copy("DJs SRL", 222L, "djs@djs.com", "Ruta", 0, "Capital")
            assertInstanceOf(Prestador::class.java, copia)
            assertEquals("DJs SRL", copia.nombre)
            assertEquals(TipoPrestador.DJ, (copia as Prestador).tipoPrestador)
        }

        @Test
        fun `copy mantiene el mismo id`() {
            val copia = empresa.copy("Otro", 111L, "a@b.com", "C", 1, "M")
            assertEquals(1L, copia.id)
        }
    }

    // ── toGenericItemDTO ──────────────────────────────────────────────────────

    @Nested
    inner class ToGenericItemDTOTest {

        @Test
        fun `toGenericItemDTO devuelve id y nombre correctos`() {
            val dto = empresa.toGenericItemDTO()
            assertEquals(1L, dto.id)
            assertEquals("Salón Test", dto.nombre)
        }
    }
}