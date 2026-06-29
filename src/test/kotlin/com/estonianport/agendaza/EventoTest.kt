package com.estonianport.agendaza

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
        extraOtro: Double = 0.0,
        descuento: Long = 0L,
        cateringOtro: Double = 0.0,
        capacidadAdultos: Int = 100,
        capacidadNinos: Int = 20,
        listaExtra: MutableSet<Extra> = mutableSetOf(),
        listaExtraVariable: MutableSet<EventoExtraVariable> = mutableSetOf()
    ): Evento = Evento(
        id = 1L,
        nombre = "fiesta de prueba",
        tipoEvento = tipoEvento,
        inicio = inicio,
        fin = fin,
        capacidadAdultos = capacidadAdultos,
        capacidadNinos = capacidadNinos,
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

            assertEquals(5000.0, buildEvento().getPresupuesto())
        }

        @Test
        fun `getPresupuesto suma extraOtro al presupuesto`() {
            whenever(empresa.getPrecioOfTipoEvento(1L, inicio)).thenReturn(5000.0)
            whenever(empresa.getSumOfPrecioByListaExtra(emptyList(), inicio)).thenReturn(0.0)
            whenever(empresa.getSumOfPrecioByListaExtraVariable(emptyList(), inicio)).thenReturn(0.0)

            assertEquals(5500.0, buildEvento(extraOtro = 500.0).getPresupuesto())
        }

        @Test
        fun `getPresupuesto aplica descuento correctamente`() {
            whenever(empresa.getPrecioOfTipoEvento(1L, inicio)).thenReturn(10000.0)
            whenever(empresa.getSumOfPrecioByListaExtra(emptyList(), inicio)).thenReturn(0.0)
            whenever(empresa.getSumOfPrecioByListaExtraVariable(emptyList(), inicio)).thenReturn(0.0)

            assertEquals(9000.0, buildEvento(descuento = 10L).getPresupuesto(), 0.001)
        }

        @Test
        fun `getPresupuesto con descuento 0 no modifica el total`() {
            whenever(empresa.getPrecioOfTipoEvento(1L, inicio)).thenReturn(3000.0)
            whenever(empresa.getSumOfPrecioByListaExtra(emptyList(), inicio)).thenReturn(0.0)
            whenever(empresa.getSumOfPrecioByListaExtraVariable(emptyList(), inicio)).thenReturn(0.0)

            assertEquals(3000.0, buildEvento(descuento = 0L).getPresupuesto())
        }

        @Test
        fun `getPresupuesto con descuento 100 da cero`() {
            whenever(empresa.getPrecioOfTipoEvento(1L, inicio)).thenReturn(8000.0)
            whenever(empresa.getSumOfPrecioByListaExtra(emptyList(), inicio)).thenReturn(0.0)
            whenever(empresa.getSumOfPrecioByListaExtraVariable(emptyList(), inicio)).thenReturn(0.0)

            assertEquals(0.0, buildEvento(descuento = 100L).getPresupuesto(), 0.001)
        }

        @Test
        fun `getPresupuesto suma extras de tipo EVENTO`() {
            val extraEvento = mock<Extra>().also { whenever(it.tipoExtra).thenReturn(TipoExtra.EVENTO) }
            val listaExtra  = mutableSetOf(extraEvento)

            whenever(empresa.getPrecioOfTipoEvento(1L, inicio)).thenReturn(1000.0)
            whenever(empresa.getSumOfPrecioByListaExtra(listOf(extraEvento), inicio)).thenReturn(500.0)
            whenever(empresa.getSumOfPrecioByListaExtraVariable(emptyList(), inicio)).thenReturn(0.0)

            assertEquals(1500.0, buildEvento(listaExtra = listaExtra).getPresupuesto(), 0.001)
        }

        @Test
        fun `getPresupuesto suma extras variables de tipo VARIABLE_EVENTO`() {
            val extraVar = mock<Extra>().also { whenever(it.tipoExtra).thenReturn(TipoExtra.VARIABLE_EVENTO) }
            val eventoExtraVar = EventoExtraVariable(1L, extraVar, 3)
            val listaVar = mutableSetOf(eventoExtraVar)

            whenever(empresa.getPrecioOfTipoEvento(1L, inicio)).thenReturn(2000.0)
            whenever(empresa.getSumOfPrecioByListaExtra(emptyList(), inicio)).thenReturn(0.0)
            whenever(empresa.getSumOfPrecioByListaExtraVariable(listOf(eventoExtraVar), inicio)).thenReturn(300.0)

            assertEquals(2300.0, buildEvento(listaExtraVariable = listaVar).getPresupuesto(), 0.001)
        }

        @Test
        fun `getPresupuesto NO suma extras de tipo TIPO_CATERING en el presupuesto base`() {
            val extraCatering = mock<Extra>().also { whenever(it.tipoExtra).thenReturn(TipoExtra.TIPO_CATERING) }
            val listaExtra = mutableSetOf(extraCatering)

            whenever(empresa.getPrecioOfTipoEvento(1L, inicio)).thenReturn(1000.0)
            // El filter de EVENTO deja lista vacía, así que suma 0 de extras
            whenever(empresa.getSumOfPrecioByListaExtra(emptyList(), inicio)).thenReturn(0.0)
            whenever(empresa.getSumOfPrecioByListaExtraVariable(emptyList(), inicio)).thenReturn(0.0)

            assertEquals(1000.0, buildEvento(listaExtra = listaExtra).getPresupuesto(), 0.001)
        }
    }

    // ── Catering ─────────────────────────────────────────────────────────────

    @Nested
    inner class CateringTest {

        @Test
        fun `getPresupuestoCatering multiplica precio catering por capacidadAdultos`() {
            val extraCatering = mock<Extra>().also { whenever(it.tipoExtra).thenReturn(TipoExtra.TIPO_CATERING) }
            val listaExtra = mutableSetOf(extraCatering)

            whenever(empresa.getSumOfPrecioByListaExtra(listOf(extraCatering), inicio)).thenReturn(200.0)
            whenever(empresa.getSumOfPrecioByListaExtraVariable(emptyList(), inicio)).thenReturn(0.0)

            // 50 adultos * 200 = 10000
            val evento = buildEvento(capacidadAdultos = 50, listaExtra = listaExtra)
            assertEquals(10000.0, evento.getPresupuestoCatering(), 0.001)
        }

        @Test
        fun `getPresupuestoCatering incluye cateringOtro por adulto`() {
            whenever(empresa.getSumOfPrecioByListaExtra(emptyList(), inicio)).thenReturn(0.0)
            whenever(empresa.getSumOfPrecioByListaExtraVariable(emptyList(), inicio)).thenReturn(0.0)

            // 40 adultos * 100 = 4000
            val evento = buildEvento(capacidadAdultos = 40, cateringOtro = 100.0)
            assertEquals(4000.0, evento.getPresupuestoCatering(), 0.001)
        }

        @Test
        fun `getPresupuestoCatering con 0 adultos da 0`() {
            whenever(empresa.getSumOfPrecioByListaExtra(emptyList(), inicio)).thenReturn(0.0)
            whenever(empresa.getSumOfPrecioByListaExtraVariable(emptyList(), inicio)).thenReturn(0.0)

            val evento = buildEvento(capacidadAdultos = 0, cateringOtro = 500.0)
            assertEquals(0.0, evento.getPresupuestoCatering(), 0.001)
        }

        @Test
        fun `getPresupuestoCatering suma extras variables de catering`() {
            val extraVar = mock<Extra>().also { whenever(it.tipoExtra).thenReturn(TipoExtra.VARIABLE_CATERING) }
            val eventoExtraVar = EventoExtraVariable(1L, extraVar, 2)
            val listaVar = mutableSetOf(eventoExtraVar)

            whenever(empresa.getSumOfPrecioByListaExtra(emptyList(), inicio)).thenReturn(0.0)
            whenever(empresa.getSumOfPrecioByListaExtraVariable(listOf(eventoExtraVar), inicio)).thenReturn(600.0)

            val evento = buildEvento(capacidadAdultos = 10, listaExtraVariable = listaVar)
            // 10*0 (cateringOtro) + 600 variable = 600
            assertEquals(600.0, evento.getPresupuestoCatering(), 0.001)
        }
    }

    // ── Totales y pagos ───────────────────────────────────────────────────────

    @Nested
    inner class TotalesYPagosTest {

        @Test
        fun `getPresupuestoTotal es suma de presupuesto y catering`() {
            whenever(empresa.getPrecioOfTipoEvento(1L, inicio)).thenReturn(2000.0)
            whenever(empresa.getSumOfPrecioByListaExtra(emptyList(), inicio)).thenReturn(0.0)
            whenever(empresa.getSumOfPrecioByListaExtraVariable(emptyList(), inicio)).thenReturn(0.0)

            // presupuesto=2000, catering=10*50=500 → total=2500
            val evento = buildEvento(capacidadAdultos = 10, cateringOtro = 50.0)
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

        @Test
        fun `getMontoFaltante puede ser negativo si se abono de mas`() {
            whenever(empresa.getPrecioOfTipoEvento(1L, inicio)).thenReturn(1000.0)
            whenever(empresa.getSumOfPrecioByListaExtra(emptyList(), inicio)).thenReturn(0.0)
            whenever(empresa.getSumOfPrecioByListaExtraVariable(emptyList(), inicio)).thenReturn(0.0)

            val evento = buildEvento()
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