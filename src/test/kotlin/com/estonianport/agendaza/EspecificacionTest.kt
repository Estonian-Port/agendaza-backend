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
    private lateinit var capacidadEvento: Capacidad
    private lateinit var capacidadTipoEvento: Capacidad

    @BeforeEach
    fun setUp() {
        empresa = mock()
        evento = mock()
        tipoEvento = mock()

        capacidadEvento = mock()
        capacidadTipoEvento = mock()

        // Configuraciones base que la mayoría de los tests van a usar
        whenever(evento.empresa).thenReturn(empresa)
        whenever(evento.tipoEvento).thenReturn(tipoEvento)
        whenever(evento.capacidad).thenReturn(capacidadEvento)
        whenever(tipoEvento.capacidad).thenReturn(capacidadTipoEvento)
    }

    // ── PrecioDePlatoNinos ──────────────────────────────────────────────────

    @Nested
    inner class PrecioDePlatoNinosTest {

        private lateinit var especificacion: PrecioDePlatoNinos

        @BeforeEach
        fun setUp() {
            especificacion = PrecioDePlatoNinos(1L, empresa, porcentaje = 50) // 50%
            whenever(capacidadEvento.capacidadNinos).thenReturn(10) // 10 Niños
        }

        @Test
        fun `aplica correctamente usando cateringOtro`() {
            // Arrange
            whenever(evento.cateringOtro).thenReturn(1000.0) // Precio plato = 1000

            // Act
            especificacion.aplicar(evento)

            // Assert
            // Cálculo: 10 niños * 1000 precio * 0.50 (50%) = 5000.0
            verify(evento).extraOtro = 5000.0
        }

        @Test
        fun `aplica correctamente usando un extra de TIPO_CATERING`() {
            // Arrange
            whenever(evento.cateringOtro).thenReturn(0.0)

            val extraCatering = mock<Extra> { whenever(it.tipoExtra).thenReturn(TipoExtra.TIPO_CATERING) }
            val extraNormal = mock<Extra> { whenever(it.tipoExtra).thenReturn(TipoExtra.EVENTO) }

            val listaExtras = mutableSetOf(extraNormal, extraCatering)
            whenever(evento.listaExtra).thenReturn(listaExtras)
            whenever(evento.inicio).thenReturn(LocalDateTime.now())

            // El mock de empresa debe devolver 2000 como precio del extra en esa fecha
            whenever(empresa.getPrecioOfExtraByFecha(eq(extraCatering), any())).thenReturn(2000.0)

            // Act
            especificacion.aplicar(evento)

            // Assert
            // Cálculo: 10 niños * 2000 precio * 0.50 (50%) = 10000.0
            verify(evento).extraOtro = 10000.0
        }

        @Test
        fun `setea extraOtro en 0 si no hay catering`() {
            // Arrange
            whenever(evento.cateringOtro).thenReturn(0.0)
            whenever(evento.listaExtra).thenReturn(mutableSetOf())

            // Act
            especificacion.aplicar(evento)

            // Assert
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
            extraNino = mock()
            especificacion = AgregarExtraNinoSiSuperaCapacidad(1L, empresa, extraNino)

            listaVariablesMock = mock()
            whenever(evento.listaEventoExtraVariable).thenReturn(listaVariablesMock)
        }

        @Test
        fun `agrega el extra variable si los ninos del evento superan los del tipo de evento`() {
            // Arrange
            whenever(capacidadEvento.capacidadNinos).thenReturn(30)
            whenever(capacidadTipoEvento.capacidadNinos).thenReturn(20)

            // Act
            especificacion.aplicar(evento)

            // Assert
            // Debería agregar un EventoExtraVariable con cantidad 10 (30 - 20)
            argumentCaptor<EventoExtraVariable>().apply {
                verify(listaVariablesMock).add(capture())
                assertEquals(10, firstValue.cantidad)
                assertEquals(extraNino, firstValue.extra)
            }
        }

        @Test
        fun `NO agrega el extra si los ninos del evento NO superan los del tipo de evento`() {
            // Arrange
            whenever(capacidadEvento.capacidadNinos).thenReturn(15)
            whenever(capacidadTipoEvento.capacidadNinos).thenReturn(20)

            // Act
            especificacion.aplicar(evento)

            // Assert
            verify(listaVariablesMock, never()).add(any())
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
            extraCamarera = mock()
            // Configurada para esperar eventos CORTOS
            especificacion = AgregarExtraCamareraSiSuperaCapacidad(1L, empresa, extraCamarera, Duracion.CORTO)

            listaVariablesMock = mock()
            whenever(evento.listaEventoExtraVariable).thenReturn(listaVariablesMock)
        }

        @Test
        fun `agrega una camarera cada 10 adultos extras si la duracion coincide`() {
            // Arrange
            whenever(tipoEvento.duracion).thenReturn(Duracion.CORTO) // Coincide
            whenever(capacidadEvento.capacidadAdultos).thenReturn(120)
            whenever(capacidadTipoEvento.capacidadAdultos).thenReturn(100)

            // Act
            especificacion.aplicar(evento)

            // Assert
            // 120 adultos - 100 base = 20 extras. 20 / 10 = 2 camareras.
            argumentCaptor<EventoExtraVariable>().apply {
                verify(listaVariablesMock).add(capture())
                assertEquals(2, firstValue.cantidad)
                assertEquals(extraCamarera, firstValue.extra)
            }
        }

        @Test
        fun `NO agrega camareras si la duracion NO coincide`() {
            // Arrange
            whenever(tipoEvento.duracion).thenReturn(Duracion.LARGO) // No coincide
            whenever(capacidadEvento.capacidadAdultos).thenReturn(150)
            whenever(capacidadTipoEvento.capacidadAdultos).thenReturn(100)

            // Act
            especificacion.aplicar(evento)

            // Assert
            verify(listaVariablesMock, never()).add(any())
        }

        @Test
        fun `NO agrega camareras si los adultos no superan la base aunque coincida la duracion`() {
            // Arrange
            whenever(tipoEvento.duracion).thenReturn(Duracion.CORTO)
            whenever(capacidadEvento.capacidadAdultos).thenReturn(90)
            whenever(capacidadTipoEvento.capacidadAdultos).thenReturn(100)

            // Act
            especificacion.aplicar(evento)

            // Assert
            verify(listaVariablesMock, never()).add(any())
        }
    }
}