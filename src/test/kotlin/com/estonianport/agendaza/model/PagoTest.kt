package com.estonianport.agendaza.model

import com.estonianport.agendaza.model.enums.Concepto
import com.estonianport.agendaza.model.enums.Duracion
import com.estonianport.agendaza.model.enums.Estado
import com.estonianport.agendaza.model.enums.MedioDePago
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class PagoTest {

    private val inicioEvento = LocalDateTime.of(2026, 12, 20, 18, 0)
    private val empresa = Salon(3L, "Salon", 123L, "salon@test.com", "Calle", 1, "Ciudad")
    private val usuario = Usuario(4L, "Ana", "Pérez", 112233L, "ana@test.com")
    private val tipoEvento = TipoEvento(5L, "Fiesta", Duracion.MEDIO, 80, 10, java.time.LocalTime.of(5, 0), empresa)
    private val evento = Evento(
        id = 6L,
        nombre = "Fiesta de fin de año",
        tipoEvento = tipoEvento,
        inicio = inicioEvento,
        fin = inicioEvento.plusHours(5),
        capacidadAdultos = 80,
        capacidadNinos = 10,
        extraOtro = 0.0,
        descuento = 0L,
        listaExtra = mutableSetOf(),
        cateringOtro = 0.0,
        cateringOtroDescripcion = "",
        encargado = usuario,
        cliente = usuario,
        codigo = "ABCD",
        estado = Estado.RESERVADO,
        anotaciones = "",
        empresa = empresa
    )

    private fun buildPago(numeroCuota: String? = null) = Pago(
        id = 7L,
        monto = 12500.0,
        concepto = Concepto.CUOTA,
        medioDePago = MedioDePago.TRANSFERENCIA,
        fecha = LocalDateTime.of(2026, 10, 2, 14, 30),
        evento = evento,
        encargado = usuario,
        numeroCuota = numeroCuota
    )

    @Test
    fun `toDTO copia los datos del pago evento empresa y encargado`() {
        val pago = buildPago(numeroCuota = "2")

        val dto = pago.toDTO()

        assertEquals(7L, dto.id)
        assertEquals(12500.0, dto.monto)
        assertEquals("ABCD", dto.codigo)
        assertEquals(MedioDePago.TRANSFERENCIA, dto.medioDePago)
        assertEquals(inicioEvento, dto.fechaEvento)
        assertEquals("Fiesta de fin de año", dto.nombreEvento)
        assertEquals(Concepto.CUOTA, dto.concepto)
        assertEquals("2", dto.numeroCuota)
        assertEquals(empresa.id, dto.empresaId)
        assertEquals(usuario.id, dto.usuarioId)
        assertEquals(pago.fecha, dto.fecha)
    }

    @Test
    fun `CUOTA describe el numero de cuota cuando esta informado`() {
        assertEquals("Cuota Nº3", Concepto.CUOTA.getDescripcion(buildPago("3")))
    }

    @Test
    fun `CUOTA devuelve descripcion general sin numero`() {
        assertEquals("Cuota", Concepto.CUOTA.getDescripcion(buildPago()))
    }
}
