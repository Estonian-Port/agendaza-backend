package com.estonianport.agendaza

import com.estonianport.agendaza.model.*
import com.estonianport.agendaza.model.enums.Duracion
import com.estonianport.agendaza.model.enums.TipoExtra
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.time.LocalDateTime

class EspecificacionTest {

    private lateinit var empresa: Empresa
    private lateinit var evento: Evento
    private lateinit var tipoEvento: TipoEvento

    @BeforeEach
    fun setUp() {
        empresa    = mock()
        evento     = mock()
        tipoEvento = mock()

        whenever(evento.empresa).thenReturn(empresa)
        whenever(evento.tipoEvento).thenReturn(tipoEvento)
    }

    // ── PrecioDePlatoNinos ────────────────────────────────────────────────────

    @Nested
    inner class PrecioDePlatoNinosTest {

        private lateinit var especificacion: PrecioDePlatoNinos

        @BeforeEach
        fun setUp() {
            especificacion = PrecioDePlatoNinos(1L, empresa, porcentaje = 50)
            // capacidadNinos ahora es campo directo en Evento
            whenever(evento.capacidadNinos).thenReturn(10)
        }

        @Test
        fun `aplica correctamente usando cateringOtro`() {
            whenever(evento.cateringOtro).thenReturn(1000.0)

            especificacion.aplicar(evento)

            // 10 niños * 1000 * 0.50 = 5000
            verify(evento).extraOtro = 5000.0
        }

        @Test
        fun `aplica correctamente usando un extra de TIPO_CATERING`() {
            whenever(evento.cateringOtro).thenReturn(0.0)

            val extraCatering = mock<Extra> { whenever(it.tipoExtra).thenReturn(TipoExtra.TIPO_CATERING) }
            val extraNormal   = mock<Extra> { whenever(it.tipoExtra).thenReturn(TipoExtra.EVENTO) }
            whenever(evento.listaExtra).thenReturn(mutableSetOf(extraNormal, extraCatering))
            whenever(evento.inicio).thenReturn(LocalDateTime.now())
            whenever(empresa.getPrecioOfExtraByFecha(eq(extraCatering), any())).thenReturn(2000.0)

            especificacion.aplicar(evento)

            // 10 niños * 2000 * 0.50 = 10000
            verify(evento).extraOtro = 10000.0
        }

        @Test
        fun `setea extraOtro en 0 si no hay catering`() {
            whenever(evento.cateringOtro).thenReturn(0.0)
            whenever(evento.listaExtra).thenReturn(mutableSetOf())

            especificacion.aplicar(evento)

            verify(evento).extraOtro = 0.0
        }

        @Test
        fun `con porcentaje 0 siempre produce extraOtro 0`() {
            val espec = PrecioDePlatoNinos(2L, empresa, porcentaje = 0)
            whenever(evento.cateringOtro).thenReturn(5000.0)

            espec.aplicar(evento)

            verify(evento).extraOtro = 0.0
        }

        @Test
        fun `con 0 ninos produce extraOtro 0 aunque haya precio`() {
            whenever(evento.capacidadNinos).thenReturn(0)
            whenever(evento.cateringOtro).thenReturn(1000.0)

            especificacion.aplicar(evento)

            verify(evento).extraOtro = 0.0
        }
    }

    // ── AgregarExtraNinoSiSuperaCapacidad ─────────────────────────────────────

    @Nested
    inner class AgregarExtraNinoSiSuperaCapacidadTest {

        private lateinit var especificacion: AgregarExtraNinoSiSuperaCapacidad
        private lateinit var extraNino: Extra
        private lateinit var listaVariablesMock: MutableSet<EventoExtraVariable>

        @BeforeEach
        fun setUp() {
            extraNino      = mock()
            especificacion = AgregarExtraNinoSiSuperaCapacidad(1L, empresa, extraNino)

            listaVariablesMock = mock()
            whenever(evento.listaEventoExtraVariable).thenReturn(listaVariablesMock)
        }

        @Test
        fun `agrega el extra si los ninos del evento superan los del tipo de evento`() {
            whenever(evento.capacidadNinos).thenReturn(30)
            whenever(tipoEvento.capacidadNinos).thenReturn(20)

            especificacion.aplicar(evento)

            argumentCaptor<EventoExtraVariable>().apply {
                verify(listaVariablesMock).add(capture())
                assertEquals(10, firstValue.cantidad)  // 30 - 20
                assertEquals(extraNino, firstValue.extra)
            }
        }

        @Test
        fun `NO agrega el extra si los ninos NO superan los del tipo de evento`() {
            whenever(evento.capacidadNinos).thenReturn(15)
            whenever(tipoEvento.capacidadNinos).thenReturn(20)

            especificacion.aplicar(evento)

            verify(listaVariablesMock, never()).add(any())
        }

        @Test
        fun `NO agrega el extra si los ninos son exactamente iguales`() {
            whenever(evento.capacidadNinos).thenReturn(20)
            whenever(tipoEvento.capacidadNinos).thenReturn(20)

            especificacion.aplicar(evento)

            verify(listaVariablesMock, never()).add(any())
        }

        @Test
        fun `la cantidad agregada es la diferencia exacta`() {
            whenever(evento.capacidadNinos).thenReturn(55)
            whenever(tipoEvento.capacidadNinos).thenReturn(40)

            especificacion.aplicar(evento)

            argumentCaptor<EventoExtraVariable>().apply {
                verify(listaVariablesMock).add(capture())
                assertEquals(15, firstValue.cantidad)
            }
        }
    }

    // ── AgregarExtraCamareraSiSuperaCapacidad ─────────────────────────────────

    @Nested
    inner class AgregarExtraCamareraSiSuperaCapacidadTest {

        private lateinit var especificacion: AgregarExtraCamareraSiSuperaCapacidad
        private lateinit var extraCamarera: Extra
        private lateinit var listaVariablesMock: MutableSet<EventoExtraVariable>

        @BeforeEach
        fun setUp() {
            extraCamarera  = mock()
            especificacion = AgregarExtraCamareraSiSuperaCapacidad(1L, empresa, extraCamarera, Duracion.CORTO)

            listaVariablesMock = mock()
            whenever(evento.listaEventoExtraVariable).thenReturn(listaVariablesMock)
        }

        @Test
        fun `agrega una camarera cada 10 adultos extras si la duracion coincide`() {
            whenever(tipoEvento.duracion).thenReturn(Duracion.CORTO)
            whenever(evento.capacidadAdultos).thenReturn(120)
            whenever(tipoEvento.capacidadAdultos).thenReturn(100)

            especificacion.aplicar(evento)

            // 120 - 100 = 20 extras → 20/10 = 2 camareras
            argumentCaptor<EventoExtraVariable>().apply {
                verify(listaVariablesMock).add(capture())
                assertEquals(2, firstValue.cantidad)
                assertEquals(extraCamarera, firstValue.extra)
            }
        }

        @Test
        fun `NO agrega camareras si la duracion NO coincide`() {
            whenever(tipoEvento.duracion).thenReturn(Duracion.LARGO)
            whenever(evento.capacidadAdultos).thenReturn(150)
            whenever(tipoEvento.capacidadAdultos).thenReturn(100)

            especificacion.aplicar(evento)

            verify(listaVariablesMock, never()).add(any())
        }

        @Test
        fun `NO agrega camareras si los adultos no superan la base`() {
            whenever(tipoEvento.duracion).thenReturn(Duracion.CORTO)
            whenever(evento.capacidadAdultos).thenReturn(90)
            whenever(tipoEvento.capacidadAdultos).thenReturn(100)

            especificacion.aplicar(evento)

            verify(listaVariablesMock, never()).add(any())
        }

        @Test
        fun `NO agrega camareras si adultos son exactamente iguales`() {
            whenever(tipoEvento.duracion).thenReturn(Duracion.CORTO)
            whenever(evento.capacidadAdultos).thenReturn(100)
            whenever(tipoEvento.capacidadAdultos).thenReturn(100)

            especificacion.aplicar(evento)

            verify(listaVariablesMock, never()).add(any())
        }

        @Test
        fun `redondea hacia abajo si el exceso no es multiplo de 10`() {
            whenever(tipoEvento.duracion).thenReturn(Duracion.CORTO)
            whenever(evento.capacidadAdultos).thenReturn(115)  // 15 extra → 1 camarera
            whenever(tipoEvento.capacidadAdultos).thenReturn(100)

            especificacion.aplicar(evento)

            argumentCaptor<EventoExtraVariable>().apply {
                verify(listaVariablesMock).add(capture())
                assertEquals(1, firstValue.cantidad)
            }
        }
    }
}