package com.estonianport.agendaza.model

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

    private lateinit var empresa: Empresa
    private lateinit var tipoEvento: TipoEvento
    private lateinit var encargado: Usuario
    private lateinit var cliente: Usuario

    private val inicio = LocalDateTime.of(2025, 6, 15, 18, 0)
    private val fin    = LocalDateTime.of(2025, 6, 15, 23, 0)

    @BeforeEach
    fun setUp() {
        empresa    = mock()
        tipoEvento = mock()
        encargado  = mock()
        cliente    = mock()

        whenever(tipoEvento.id).thenReturn(1L)
    }

    private fun buildEvento(
        precioTipoEvento: Double = 0.0,
        extraOtro: Double = 0.0,
        extraOtroDescripcion: String = "",
        cateringOtro: Double = 0.0,
        capacidadAdultos: Int = 100,
        capacidadNinos: Int = 20,
        listaExtra: MutableSet<EventoExtra> = mutableSetOf(),
    ): Evento = Evento(
        id = 1L,
        nombre = "fiesta de prueba",
        tipoEvento = tipoEvento,
        inicio = inicio,
        fin = fin,
        capacidadAdultos = capacidadAdultos,
        capacidadNinos = capacidadNinos,
        precioTipoEvento = precioTipoEvento,
        extraOtro = extraOtro,
        extraOtroDescripcion = extraOtroDescripcion,
        listaEventoExtra = listaExtra,
        cateringOtro = cateringOtro,
        cateringOtroDescripcion = "",
        encargado = encargado,
        cliente = cliente,
        codigo = "ABCD",
        estado = Estado.RESERVADO,
        anotaciones = "",
        empresa = empresa
    )

    private fun eventoExtra(
        tipoExtra: TipoExtra,
        precio: Double,
        cantidad: Int = 1
    ): EventoExtra {
        val extra = mock<Extra>().also { whenever(it.tipoExtra).thenReturn(tipoExtra) }
        return EventoExtra(1L, buildEvento(), extra, precio, cantidad)
    }

    // ── Presupuesto base ─────────────────────────────────────────────────────

    @Nested
    inner class PresupuestoTest {

        @Test
        fun `getPresupuesto devuelve solo precio tipo evento cuando no hay extras`() {
            assertEquals(5000.0, buildEvento(precioTipoEvento = 5000.0).getPresupuesto())
        }

        @Test
        fun `getPresupuesto suma extraOtro al presupuesto`() {
            assertEquals(5500.0, buildEvento(precioTipoEvento = 5000.0, extraOtro = 500.0).getPresupuesto())
        }

        @Test
        fun `getPresupuesto suma extras de tipo EVENTO`() {
            val extraEvento = eventoExtra(TipoExtra.EVENTO, precio = 500.0)
            val listaExtra  = mutableSetOf(extraEvento)

            assertEquals(1500.0, buildEvento(precioTipoEvento = 1000.0, listaExtra = listaExtra).getPresupuesto(), 0.001)
        }

        @Test
        fun `getPresupuesto usa precio por cantidad para extras variables de evento`() {
            val extraVariable = eventoExtra(TipoExtra.VARIABLE_EVENTO, precio = 300.0, cantidad = 3)
            val evento = buildEvento(precioTipoEvento = 2000.0, listaExtra = mutableSetOf(extraVariable))

            assertEquals(2900.0, evento.getPresupuesto(), 0.001)
        }

        @Test
        fun `getPresupuesto suma extras variables de tipo VARIABLE_EVENTO`() {
            val eventoExtraVar = eventoExtra(TipoExtra.VARIABLE_EVENTO, precio = 2300.0)
            val listaVar = mutableSetOf(eventoExtraVar)

            assertEquals(4300.0, buildEvento(precioTipoEvento = 2000.0, listaExtra = listaVar).getPresupuesto(), 0.001)
        }

        @Test
        fun `getPresupuesto NO suma extras de tipo TIPO_CATERING en el presupuesto base`() {
            val extraCatering = eventoExtra(TipoExtra.TIPO_CATERING, precio = 500.0)
            val listaExtra = mutableSetOf(extraCatering)

            assertEquals(1000.0, buildEvento(precioTipoEvento = 1000.0, listaExtra = listaExtra).getPresupuesto(), 0.001)
        }
    }

    // ── Catering ─────────────────────────────────────────────────────────────

    @Nested
    inner class CateringTest {

        @Test
        fun `getPresupuestoCatering multiplica precio catering por capacidadAdultos`() {
            val extraCatering = eventoExtra(TipoExtra.TIPO_CATERING, precio = 200.0)
            val listaExtra = mutableSetOf(extraCatering)

            // 50 adultos * 200 = 10000
            val evento = buildEvento(capacidadAdultos = 50, listaExtra = listaExtra)
            assertEquals(10000.0, evento.getPresupuestoCatering(), 0.001)
        }

        @Test
        fun `getPresupuestoCatering incluye cateringOtro por adulto`() {
            // 40 adultos * 100 = 4000
            val evento = buildEvento(capacidadAdultos = 40, cateringOtro = 100.0)
            assertEquals(4000.0, evento.getPresupuestoCatering(), 0.001)
        }

        @Test
        fun `getPresupuestoCatering suma extras variables de catering`() {
            val eventoExtraVar = eventoExtra(TipoExtra.VARIABLE_CATERING, precio = 300.0, cantidad = 2)
            val listaVar = mutableSetOf(eventoExtraVar)

            val evento = buildEvento(capacidadAdultos = 10, listaExtra = listaVar)
            // 10*0 (cateringOtro) + 600 variable = 600
            assertEquals(600.0, evento.getPresupuestoCatering(), 0.001)
        }

        @Test
        fun `getPresupuestoCatering multiplica extras fijos de catering por capacidad`() {
            val extraCatering = eventoExtra(TipoExtra.TIPO_CATERING, precio = 125.0, cantidad = 2)
            val evento = buildEvento(capacidadAdultos = 8, listaExtra = mutableSetOf(extraCatering))

            assertEquals(2000.0, evento.getPresupuestoCatering(), 0.001)
        }
    }

    // ── Totales y pagos ───────────────────────────────────────────────────────

    @Nested
    inner class TotalesYPagosTest {

        @Test
        fun `getPresupuestoTotal es suma de presupuesto y catering`() {
            // presupuesto=2000, catering=10*50=500 → total=2500
            val evento = buildEvento(precioTipoEvento = 2000.0, capacidadAdultos = 10, cateringOtro = 50.0)
            assertEquals(2500.0, evento.getPresupuestoTotal(), 0.001)
        }

        @Test
        fun `presupuesto total combina extras base y catering variable`() {
            val extraEvento = eventoExtra(TipoExtra.EVENTO, precio = 250.0)
            val extraVariableCatering = eventoExtra(TipoExtra.VARIABLE_CATERING, precio = 40.0, cantidad = 3)
            val evento = buildEvento(
                precioTipoEvento = 1000.0,
                extraOtro = 50.0,
                capacidadAdultos = 5,
                listaExtra = mutableSetOf(extraEvento, extraVariableCatering)
            )

            // 1000 + 250 + 50 + (3*40) = 1420
            assertEquals(1420.0, evento.getPresupuestoTotal(), 0.001)
        }

        @Test
        fun `presupuesto total incluye cateringOtro junto con extras base y variables`() {
            val extraEvento = eventoExtra(TipoExtra.EVENTO, precio = 250.0)
            val extraVariableEvento = eventoExtra(TipoExtra.VARIABLE_EVENTO, precio = 10.0, cantidad = 2)
            val extraTipoCatering = eventoExtra(TipoExtra.TIPO_CATERING, precio = 30.0)
            val extraVariableCatering = eventoExtra(TipoExtra.VARIABLE_CATERING, precio = 40.0, cantidad = 3)
            val evento = buildEvento(
                precioTipoEvento = 1000.0,
                extraOtro = 50.0,
                capacidadAdultos = 5,
                cateringOtro = 20.0,
                listaExtra = mutableSetOf(
                    extraEvento,
                    extraVariableEvento,
                    extraTipoCatering,
                    extraVariableCatering
                )
            )

            // Base: 1000 + 250 + (10*2) + 50 = 1320
            // Catering: (5*30) + (40*3) + (5*20) = 370
            assertEquals(1690.0, evento.getPresupuestoTotal(), 0.001)
        }

        @Test
        fun `getTotalAbonado suma solo pagos activos`() {
            val evento = buildEvento()
            val pagoActivo = mock<Pago>().also {
                whenever(it.monto).thenReturn(1000.0)
                whenever(it.fechaBaja).thenReturn(null)
            }
            val pagoBaja = mock<Pago>().also {
                whenever(it.monto).thenReturn(500.0)
                whenever(it.fechaBaja).thenReturn(LocalDate.now())
            }
            evento.listaPago = mutableSetOf(pagoActivo, pagoBaja)

            assertEquals(1000.0, evento.getTotalAbonado())
        }

        @Test
        fun `getTotalAbonado con lista vacia devuelve 0`() {
            val evento = buildEvento()
            assertEquals(0.0, evento.getTotalAbonado())
        }

        @Test
        fun `getTotalAbonado con todos los pagos dados de baja devuelve 0`() {
            val evento = buildEvento()
            val pagoBaja = mock<Pago>().also {
                whenever(it.monto).thenReturn(999.0)
                whenever(it.fechaBaja).thenReturn(LocalDate.now())
            }
            evento.listaPago = mutableSetOf(pagoBaja)

            assertEquals(0.0, evento.getTotalAbonado())
        }

        @Test
        fun `getMontoFaltante es presupuestoTotal menos totalAbonado`() {
            val evento = buildEvento(precioTipoEvento = 5000.0)
            val pago = mock<Pago>().also {
                whenever(it.monto).thenReturn(2000.0)
                whenever(it.fechaBaja).thenReturn(null)
            }
            evento.listaPago = mutableSetOf(pago)

            assertEquals(3000.0, evento.getMontoFaltante(), 0.001)
        }

        @Test
        fun `getMontoFaltante es 0 cuando se abono el total exacto`() {
            val evento = buildEvento(precioTipoEvento = 1500.0)
            val pago = mock<Pago>().also {
                whenever(it.monto).thenReturn(1500.0)
                whenever(it.fechaBaja).thenReturn(null)
            }
            evento.listaPago = mutableSetOf(pago)

            assertEquals(0.0, evento.getMontoFaltante(), 0.001)
        }

        @Test
        fun `getMontoFaltante puede ser negativo si se abono de mas`() {
            val evento = buildEvento(precioTipoEvento = 1000.0)
            val pago = mock<Pago>().also {
                whenever(it.monto).thenReturn(1500.0)
                whenever(it.fechaBaja).thenReturn(null)
            }
            evento.listaPago = mutableSetOf(pago)

            assertEquals(-500.0, evento.getMontoFaltante(), 0.001)
        }
    }

    // ── Estado y campos simples ───────────────────────────────────────────────

    @Nested
    inner class EstadoYCamposTest {

        @Test
        fun `fechaBaja es null por defecto al crear el evento`() {
            assertNull(buildEvento().fechaBaja)
        }

        @Test
        fun `listaEmpleado es un set vacio por defecto`() {
            assertTrue(buildEvento().listaEmpleado.isEmpty())
        }

        @Test
        fun `listaPago es un set vacio por defecto`() {
            assertTrue(buildEvento().listaPago.isEmpty())
        }

        @Test
        fun `capacidadAdultos y capacidadNinos se guardan correctamente`() {
            val evento = buildEvento(capacidadAdultos = 75, capacidadNinos = 15)
            assertEquals(75, evento.capacidadAdultos)
            assertEquals(15, evento.capacidadNinos)
        }
    }
}
