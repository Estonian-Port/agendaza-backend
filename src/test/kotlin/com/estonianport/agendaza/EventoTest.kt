package com.estonianport.agendaza

import com.estonianport.agendaza.model.Capacidad
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Evento
import com.estonianport.agendaza.model.EventoExtraVariable
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.Pago
import com.estonianport.agendaza.model.TipoEvento
import com.estonianport.agendaza.model.Usuario
import com.estonianport.agendaza.model.enums.Estado
import com.estonianport.agendaza.model.enums.TipoExtra
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime

class EventoTest {

    // ── Helpers ──────────────────────────────────────────────────────────────

    private lateinit var empresa: Empresa
    private lateinit var tipoEvento: TipoEvento
    private lateinit var encargado: Usuario
    private lateinit var cliente: Usuario
    private lateinit var capacidad: Capacidad

    private val inicio = LocalDateTime.of(2025, 6, 15, 18, 0)
    private val fin    = LocalDateTime.of(2025, 6, 15, 23, 0)

    @BeforeEach
    fun setUp() {
        empresa    = mock()
        tipoEvento = mock()
        encargado  = mock()
        cliente    = mock()
        capacidad  = Capacidad(1L, 100, 20)

        whenever(tipoEvento.id).thenReturn(1L)
    }

    private fun buildEvento(
        extraOtro: Double = 0.0,
        descuento: Long = 0L,
        cateringOtro: Double = 0.0,
        listaExtra: MutableSet<Extra> = mutableSetOf(),
        listaExtraVariable: MutableSet<EventoExtraVariable> = mutableSetOf()
    ): Evento = Evento(
        id = 1L,
        nombre = "fiesta de prueba",
        tipoEvento = tipoEvento,
        inicio = inicio,
        fin = fin,
        capacidad = capacidad,
        extraOtro = extraOtro,
        descuento = descuento,
        listaExtra = listaExtra,
        listaEventoExtraVariable = listaExtraVariable,
        cateringOtro = cateringOtro,
        cateringOtroDescripcion = "",
        encargado = encargado,
        cliente = cliente,
        codigo = "ABCD",
        estado = Estado.RESERVADO,
        anotaciones = "",
        empresa = empresa
    )

    // ── Presupuesto base ─────────────────────────────────────────────────────

    @Nested
    inner class PresupuestoTest {

        @Test
        fun `getPresupuesto devuelve solo precio tipo evento cuando no hay extras`() {
            whenever(empresa.getPrecioOfTipoEvento(1L, inicio)).thenReturn(5000.0)
            whenever(empresa.getSumOfPrecioByListaExtra(emptyList(), inicio)).thenReturn(0.0)
            whenever(empresa.getSumOfPrecioByListaExtraVariable(emptyList(), inicio)).thenReturn(0.0)

            val evento = buildEvento()
            assertEquals(5000.0, evento.getPresupuesto())
        }

        @Test
        fun `getPresupuesto suma extraOtro al presupuesto`() {
            whenever(empresa.getPrecioOfTipoEvento(1L, inicio)).thenReturn(5000.0)
            whenever(empresa.getSumOfPrecioByListaExtra(emptyList(), inicio)).thenReturn(0.0)
            whenever(empresa.getSumOfPrecioByListaExtraVariable(emptyList(), inicio)).thenReturn(0.0)

            val evento = buildEvento(extraOtro = 500.0)
            assertEquals(5500.0, evento.getPresupuesto())
        }

        @Test
        fun `getPresupuesto aplica descuento correctamente`() {
            whenever(empresa.getPrecioOfTipoEvento(1L, inicio)).thenReturn(10000.0)
            whenever(empresa.getSumOfPrecioByListaExtra(emptyList(), inicio)).thenReturn(0.0)
            whenever(empresa.getSumOfPrecioByListaExtraVariable(emptyList(), inicio)).thenReturn(0.0)

            // 10% de descuento → 9000
            val evento = buildEvento(descuento = 10L)
            assertEquals(9000.0, evento.getPresupuesto(), 0.001)
        }

        @Test
        fun `getPresupuesto con descuento 0 no modifica el total`() {
            whenever(empresa.getPrecioOfTipoEvento(1L, inicio)).thenReturn(3000.0)
            whenever(empresa.getSumOfPrecioByListaExtra(emptyList(), inicio)).thenReturn(0.0)
            whenever(empresa.getSumOfPrecioByListaExtraVariable(emptyList(), inicio)).thenReturn(0.0)

            val evento = buildEvento(descuento = 0L)
            assertEquals(3000.0, evento.getPresupuesto())
        }

        @Test
        fun `getPresupuesto con descuento 100 da cero`() {
            whenever(empresa.getPrecioOfTipoEvento(1L, inicio)).thenReturn(8000.0)
            whenever(empresa.getSumOfPrecioByListaExtra(emptyList(), inicio)).thenReturn(0.0)
            whenever(empresa.getSumOfPrecioByListaExtraVariable(emptyList(), inicio)).thenReturn(0.0)

            val evento = buildEvento(descuento = 100L)
            assertEquals(0.0, evento.getPresupuesto(), 0.001)
        }
    }

    // ── Catering ─────────────────────────────────────────────────────────────

    @Nested
    inner class CateringTest {

        @Test
        fun `getPresupuestoCatering multiplica precio catering por capacidad adultos`() {
            capacidad = Capacidad(1L, 50, 10)  // 50 adultos

            // extras tipo TIPO_CATERING → precio 200 c/u → 50 * 200 = 10000
            val extraCatering = mock<Extra>().also { whenever(it.tipoExtra).thenReturn(TipoExtra.TIPO_CATERING) }
            val listaExtra = mutableSetOf(extraCatering)

            whenever(empresa.getSumOfPrecioByListaExtra(listOf(extraCatering), inicio)).thenReturn(200.0)
            whenever(empresa.getSumOfPrecioByListaExtraVariable(emptyList(), inicio)).thenReturn(0.0)

            val evento = buildEvento(listaExtra = listaExtra, cateringOtro = 0.0).also {
                it.capacidad = capacidad
            }

            assertEquals(10000.0, evento.getPresupuestoCatering(), 0.001)
        }

        @Test
        fun `getPresupuestoCatering incluye cateringOtro por adulto`() {
            capacidad = Capacidad(1L, 40, 0)

            whenever(empresa.getSumOfPrecioByListaExtra(emptyList(), inicio)).thenReturn(0.0)
            whenever(empresa.getSumOfPrecioByListaExtraVariable(emptyList(), inicio)).thenReturn(0.0)

            val evento = buildEvento(cateringOtro = 100.0).also { it.capacidad = capacidad }

            // 40 * 100 = 4000
            assertEquals(4000.0, evento.getPresupuestoCatering(), 0.001)
        }
    }

    // ── Totales y pagos ───────────────────────────────────────────────────────

    @Nested
    inner class TotalesYPagosTest {

        @Test
        fun `getPresupuestoTotal es suma de presupuesto y catering`() {
            capacidad = Capacidad(1L, 10, 0)
            whenever(empresa.getPrecioOfTipoEvento(1L, inicio)).thenReturn(2000.0)
            whenever(empresa.getSumOfPrecioByListaExtra(emptyList(), inicio)).thenReturn(0.0)
            whenever(empresa.getSumOfPrecioByListaExtraVariable(emptyList(), inicio)).thenReturn(0.0)

            val evento = buildEvento(cateringOtro = 50.0).also { it.capacidad = capacidad }
            // presupuesto=2000, catering=10*50=500 → total=2500
            assertEquals(2500.0, evento.getPresupuestoTotal(), 0.001)
        }

        @Test
        fun `getTotalAbonado suma solo pagos activos`() {
            whenever(empresa.getPrecioOfTipoEvento(1L, inicio)).thenReturn(0.0)
            whenever(empresa.getSumOfPrecioByListaExtra(emptyList(), inicio)).thenReturn(0.0)
            whenever(empresa.getSumOfPrecioByListaExtraVariable(emptyList(), inicio)).thenReturn(0.0)

            val evento = buildEvento()

            val pagoActivo = mock<Pago>().also {
                whenever(it.monto).thenReturn(1000.0)
                whenever(it.fechaBaja).thenReturn(null)
            }
            val pagoDadoDeBaja = mock<Pago>().also {
                whenever(it.monto).thenReturn(500.0)
                whenever(it.fechaBaja).thenReturn(LocalDate.now())
            }

            evento.listaPago = mutableSetOf(pagoActivo, pagoDadoDeBaja)

            assertEquals(1000.0, evento.getTotalAbonado())
        }

        @Test
        fun `getMontoFaltante es presupuestoTotal menos totalAbonado`() {
            whenever(empresa.getPrecioOfTipoEvento(1L, inicio)).thenReturn(5000.0)
            whenever(empresa.getSumOfPrecioByListaExtra(emptyList(), inicio)).thenReturn(0.0)
            whenever(empresa.getSumOfPrecioByListaExtraVariable(emptyList(), inicio)).thenReturn(0.0)

            val evento = buildEvento()
            val pago = mock<Pago>().also {
                whenever(it.monto).thenReturn(2000.0)
                whenever(it.fechaBaja).thenReturn(null)
            }
            evento.listaPago = mutableSetOf(pago)

            assertEquals(3000.0, evento.getMontoFaltante(), 0.001)
        }

        @Test
        fun `getMontoFaltante es 0 cuando se abono el total exacto`() {
            whenever(empresa.getPrecioOfTipoEvento(1L, inicio)).thenReturn(1500.0)
            whenever(empresa.getSumOfPrecioByListaExtra(emptyList(), inicio)).thenReturn(0.0)
            whenever(empresa.getSumOfPrecioByListaExtraVariable(emptyList(), inicio)).thenReturn(0.0)

            val evento = buildEvento()
            val pago = mock<Pago>().also {
                whenever(it.monto).thenReturn(1500.0)
                whenever(it.fechaBaja).thenReturn(null)
            }
            evento.listaPago = mutableSetOf(pago)

            assertEquals(0.0, evento.getMontoFaltante(), 0.001)
        }
    }
}