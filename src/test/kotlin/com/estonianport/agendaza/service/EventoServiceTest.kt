package com.estonianport.agendaza.service

import com.estonianport.agendaza.common.emailService.EmailService
import com.estonianport.agendaza.common.openPDF.PdfService
import com.estonianport.agendaza.dto.EventoDTO
import com.estonianport.agendaza.dto.EventoReservaDTO
import com.estonianport.agendaza.dto.EventoAgendaDTO
import com.estonianport.agendaza.dto.EventoCapacidadDTO
import com.estonianport.agendaza.dto.EventoCateringDTO
import com.estonianport.agendaza.dto.EventoConUsuarioDTO
import com.estonianport.agendaza.dto.EventoExtraDTO
import com.estonianport.agendaza.dto.ExtraDTO
import com.estonianport.agendaza.dto.EventoExtraVariableDTO
import com.estonianport.agendaza.dto.EventoHoraDTO
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Evento
import com.estonianport.agendaza.model.EventoExtraVariable
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.Salon
import com.estonianport.agendaza.model.TipoEvento
import com.estonianport.agendaza.model.Usuario
import com.estonianport.agendaza.model.enums.Estado
import com.estonianport.agendaza.model.enums.TipoExtra
import com.estonianport.agendaza.repository.EventoRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.format.DateTimeParseException
import java.time.LocalDateTime
import java.util.Optional

class EventoServiceTest {

    private val eventoRepository     = mock<EventoRepository>()
    private val empresaService       = mock<EmpresaService>()
    private val extraService         = mock<ExtraService>()
    private val extraVariableService = mock<ExtraVariableService>()
    private val emailService         = mock<EmailService>()
    private val usuarioService       = mock<UsuarioService>()
    private val pdfService           = mock<PdfService>()
    private val tipoEventoService    = mock<TipoEventoService>()

    private lateinit var service: EventoService

    @BeforeEach
    fun setUp() {
        service = EventoService(
            eventoRepository, empresaService, extraService, extraVariableService,
            emailService, usuarioService, pdfService, tipoEventoService
        )
    }

    private fun buildEvento(id: Long = 1L, nombre: String = "evento test"): Evento {
        val empresa    = Salon(1L, "Salon test", 123456789L, "salon@test.com", "Calle 1", 1, "Ciudad")
        val tipoEvento = mock<TipoEvento>().also {
            whenever(it.id).thenReturn(1L)
            whenever(it.nombre).thenReturn("Casamiento")
        }
        val encargado  = Usuario(2L, "Encargado", "Prueba", 111111111L, "encargado@test.com")
        val cliente    = Usuario(3L, "Cliente", "Prueba", 222222222L, "cliente@test.com")

        return Evento(
            id = id, nombre = nombre, tipoEvento = tipoEvento,
            inicio = LocalDateTime.now(), fin = LocalDateTime.now().plusHours(5),
            capacidadAdultos = 100, capacidadNinos = 20,
            extraOtro = 0.0, descuento = 0L,
            listaExtra = mutableSetOf(), cateringOtro = 0.0,
            cateringOtroDescripcion = "", encargado = encargado, cliente = cliente,
            codigo = "ABCD", estado = Estado.RESERVADO, anotaciones = "", empresa = empresa
        )
    }

    private fun buildEventoDTO(id: Long = 1L, nombre: String = "evento test"): EventoDTO {
        return EventoDTO(
            id = id, nombre = nombre, codigo = "ABCD",
            inicio = LocalDateTime.now(), fin = LocalDateTime.now().plusHours(5),
            tipoEvento = "Casamiento"
        )
    }

    private fun buildReservaDTO(cliente: Usuario = Usuario(0L, " Ana ", " Perez ", 123456789L, " ANA@TEST.COM "), codigo: String = "ABCD") =
        EventoReservaDTO(
            id = 0L, nombre = "  Fiesta de Ana  ", capacidadAdultos = 50, capacidadNinos = 10,
            codigo = codigo, inicio = LocalDateTime.of(2026, 10, 10, 18, 0),
            fin = LocalDateTime.of(2026, 10, 10, 23, 0), tipoEventoId = 4L, empresaId = 3L,
            extraOtro = 0.0, descuento = 0L, listaExtra = emptyList(), listaExtraVariable = emptyList(),
            cateringOtro = 0.0, cateringOtroDescripcion = "", listaExtraTipoCatering = emptyList(),
            listaExtraCateringVariable = emptyList(), cliente = cliente, encargadoId = 5L,
            estado = Estado.RESERVADO, anotaciones = ""
        )

    private fun prepararDependenciasReserva(dto: EventoReservaDTO) {
        whenever(empresaService.findById(dto.empresaId)).thenReturn(mock())
        whenever(extraService.fromListaExtraDtoToListaExtra(any())).thenReturn(emptyList())
        whenever(extraVariableService.fromListaExtraVariableDtoToListaExtraVariable(any())).thenReturn(emptyList())
        whenever(usuarioService.getByEmail(any())).thenReturn(null)
        whenever(usuarioService.getByCelular(any())).thenReturn(null)
        whenever(usuarioService.save(any())).thenAnswer { it.arguments[0] as Usuario }
        whenever(tipoEventoService.get(dto.tipoEventoId)).thenReturn(mock())
        whenever(usuarioService.findById(dto.encargadoId)).thenReturn(mock())
        whenever(eventoRepository.save(any<Evento>())).thenAnswer { invocation ->
            (invocation.arguments[0] as Evento).also { it.id = 42L }
        }
        whenever(emailService.isEmailValid(any())).thenReturn(false)
    }

    @Nested
    inner class RegistrarReservaTest {

        @Test
        fun `registra reserva normaliza datos y vincula extras variables`() {
            val dto = buildReservaDTO().copy(
                listaExtra = listOf(ExtraDTO(10L, "extra", TipoExtra.EVENTO)),
                listaExtraTipoCatering = listOf(ExtraDTO(11L, "catering", TipoExtra.TIPO_CATERING)),
                listaExtraVariable = listOf(EventoExtraVariableDTO(20L, 2, "variable", 15.0)),
                listaExtraCateringVariable = listOf(EventoExtraVariableDTO(21L, 3, "variable catering", 20.0))
            )
            val extraEvento = mock<Extra>()
            val extraCatering = mock<Extra>()
            val variableEvento = EventoExtraVariable(1L, mock(), 2)
            val variableCatering = EventoExtraVariable(2L, mock(), 3)
            prepararDependenciasReserva(dto)
            whenever(extraService.fromListaExtraDtoToListaExtra(dto.listaExtra)).thenReturn(listOf(extraEvento))
            whenever(extraService.fromListaExtraDtoToListaExtra(dto.listaExtraTipoCatering)).thenReturn(listOf(extraCatering))
            whenever(extraVariableService.fromListaExtraVariableDtoToListaExtraVariable(dto.listaExtraVariable)).thenReturn(listOf(variableEvento))
            whenever(extraVariableService.fromListaExtraVariableDtoToListaExtraVariable(dto.listaExtraCateringVariable)).thenReturn(listOf(variableCatering))
            whenever(emailService.isEmailValid("ana@test.com")).thenReturn(true)

            assertEquals(42L, service.registrarReserva(dto))

            val captor = argumentCaptor<Evento>()
            verify(eventoRepository).save(captor.capture())
            val saved = captor.firstValue
            assertEquals("fiesta de ana", saved.nombre)
            assertEquals(setOf(extraEvento, extraCatering), saved.listaExtra)
            assertEquals(setOf(variableEvento, variableCatering), saved.listaEventoExtraVariable)
            assertSame(saved, variableEvento.evento)
            assertSame(saved, variableCatering.evento)
            assertEquals("ana", dto.cliente.nombre)
            assertEquals("perez", dto.cliente.apellido)
            assertEquals("ana@test.com", dto.cliente.email)
            verify(usuarioService).save(dto.cliente)
            verify(emailService).enviarMailComprabanteReserva(saved, "sido reservado", saved.empresa)
        }

        @Test
        fun `reutiliza cliente existente por id`() {
            val clienteSolicitado = Usuario(9L, "Original", "Cliente", 111L, "original@test.com")
            val dto = buildReservaDTO(clienteSolicitado)
            val clienteExistente = Usuario(9L, "Ana", "Perez", 222L, "ana@test.com")
            prepararDependenciasReserva(dto)
            whenever(usuarioService.get(9L)).thenReturn(clienteExistente)

            service.registrarReserva(dto)

            verify(usuarioService).get(9L)
            verify(usuarioService, never()).save(any())
            verify(eventoRepository).save(argThat { cliente === clienteExistente })
        }

        @Test
        fun `reutiliza cliente por email normalizado`() {
            val dto = buildReservaDTO(Usuario(0L, "Ana", "Perez", 123L, " ANA@TEST.COM "))
            val existente = Usuario(8L, "Ana", "Perez", 123L, "ana@test.com")
            prepararDependenciasReserva(dto)
            whenever(usuarioService.getByEmail("ana@test.com")).thenReturn(existente)

            service.registrarReserva(dto)

            verify(eventoRepository).save(argThat { cliente === existente })
            verify(usuarioService, never()).save(any())
        }

        @Test
        fun `reutiliza cliente por celular cuando no coincide email`() {
            val dto = buildReservaDTO(Usuario(0L, "Ana", "Perez", 123L, "otro@test.com"))
            val existente = Usuario(8L, "Ana", "Perez", 123L, "ana@test.com")
            prepararDependenciasReserva(dto)
            whenever(usuarioService.getByCelular(123L)).thenReturn(existente)

            service.registrarReserva(dto)

            verify(usuarioService).getByCelular(123L)
            verify(eventoRepository).save(argThat { cliente === existente })
            verify(usuarioService, never()).save(any())
        }

        @Test
        fun `crea cliente con datos por defecto y celular fantasma libre`() {
            val cliente = Usuario(0L, "  ", "  ", 0L, " ")
            val dto = buildReservaDTO(cliente)
            prepararDependenciasReserva(dto)
            whenever(usuarioService.getByCelular(any())).thenReturn(null)

            service.registrarReserva(dto)

            assertEquals("cliente", cliente.nombre)
            assertEquals("cliente", cliente.apellido)
            assertTrue(cliente.email.startsWith("sin-email-") && cliente.email.endsWith("@agendaza.com"))
            assertTrue(cliente.celular.toString().startsWith("999"))
            assertNull(cliente.username)
            assertNull(cliente.password)
            verify(usuarioService).save(cliente)
        }

        @Test
        fun `reintenta con email existente ante conflicto de integridad`() {
            val dto = buildReservaDTO()
            val existente = Usuario(8L, "Ana", "Perez", 123L, "ana@test.com")
            prepararDependenciasReserva(dto)
            whenever(usuarioService.save(any())).thenThrow(DataIntegrityViolationException("duplicado"))
            whenever(usuarioService.getByEmail("ana@test.com")).thenReturn(existente)

            service.registrarReserva(dto)

            verify(eventoRepository).save(argThat { cliente === existente })
        }

        @Test
        fun `propaga conflicto de integridad si no encuentra cliente existente`() {
            val dto = buildReservaDTO()
            prepararDependenciasReserva(dto)
            whenever(usuarioService.save(any())).thenThrow(DataIntegrityViolationException("duplicado"))

            assertThrows(DataIntegrityViolationException::class.java) { service.registrarReserva(dto) }
            verify(eventoRepository, never()).save(any())
        }

        @Test
        fun `genera codigo unico cuando el dto no trae codigo`() {
            val dto = buildReservaDTO(codigo = "  ")
            prepararDependenciasReserva(dto)
            whenever(eventoRepository.existCodigoInEmpresa(any(), any())).thenReturn(true, false)

            service.registrarReserva(dto)

            val captor = argumentCaptor<Evento>()
            verify(eventoRepository).save(captor.capture())
            assertTrue(captor.firstValue.codigo.matches(Regex("[A-Z]{4}")))
            verify(eventoRepository, atLeast(2)).existCodigoInEmpresa(any(), any())
        }

        @Test
        fun `lanza not found si el tipo de evento no existe`() {
            val dto = buildReservaDTO()
            prepararDependenciasReserva(dto)
            whenever(tipoEventoService.get(dto.tipoEventoId)).thenReturn(null)

            assertThrows(NotFoundException::class.java) { service.registrarReserva(dto) }
            verify(eventoRepository, never()).save(any())
        }

        @Test
        fun `lanza not found si el cliente indicado por id no existe`() {
            val dto = buildReservaDTO(Usuario(12L, "Ana", "Perez", 123L, "ana@test.com"))
            prepararDependenciasReserva(dto)
            whenever(usuarioService.get(12L)).thenReturn(null)

            assertThrows(NotFoundException::class.java) { service.registrarReserva(dto) }
            verify(eventoRepository, never()).save(any())
        }

        @Test
        fun `reintenta conflicto de integridad usando celular si no hay email coincidente`() {
            val dto = buildReservaDTO(Usuario(0L, "Ana", "Perez", 123L, "ana@test.com"))
            val existente = Usuario(8L, "Ana", "Perez", 123L, "otro@test.com")
            prepararDependenciasReserva(dto)
            whenever(usuarioService.save(any())).thenThrow(DataIntegrityViolationException("duplicado"))
            whenever(usuarioService.getByEmail("ana@test.com")).thenReturn(null)
            whenever(usuarioService.getByCelular(123L)).thenReturn(existente)

            service.registrarReserva(dto)

            verify(eventoRepository).save(argThat { cliente === existente })
        }

        @Test
        fun `un error al notificar no deshace la reserva`() {
            val dto = buildReservaDTO()
            prepararDependenciasReserva(dto)
            whenever(emailService.isEmailValid("ana@test.com")).thenReturn(true)
            whenever(emailService.enviarMailComprabanteReserva(any(), any(), any())).thenThrow(RuntimeException("smtp caido"))

            assertEquals(42L, service.registrarReserva(dto))
            verify(eventoRepository).save(any())
        }
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Nested
    inner class FindByIdTest {

        @Test
        fun `findById devuelve evento cuando existe`() {
            val evento = buildEvento()
            whenever(eventoRepository.findById(1L)).thenReturn(Optional.of(evento))
            assertEquals(evento, service.findById(1L))
        }

        @Test
        fun `findById lanza excepcion cuando no existe`() {
            whenever(eventoRepository.findById(99L)).thenReturn(Optional.empty())
            assertThrows(NotFoundException::class.java) { service.findById(99L) }
        }
    }

    // ── getHorarioDisponible ──────────────────────────────────────────────────

    @Nested
    inner class HorarioDisponibleTest {

        private val desde = LocalDateTime.of(2025, 10, 1, 18, 0)
        private val hasta = LocalDateTime.of(2025, 10, 1, 23, 0)

        @Test
        fun `devuelve true cuando el horario esta libre`() {
            whenever(eventoRepository.existeSuperposicionDeHorarios(1L, desde, hasta)).thenReturn(false)
            assertTrue(service.getHorarioDisponible(1L, desde, hasta))
        }

        @Test
        fun `devuelve false cuando hay superposicion`() {
            whenever(eventoRepository.existeSuperposicionDeHorarios(1L, desde, hasta)).thenReturn(true)
            assertFalse(service.getHorarioDisponible(1L, desde, hasta))
        }

        @Test
        fun `empresas distintas no se interfieren entre si`() {
            whenever(eventoRepository.existeSuperposicionDeHorarios(1L, desde, hasta)).thenReturn(true)
            whenever(eventoRepository.existeSuperposicionDeHorarios(2L, desde, hasta)).thenReturn(false)

            assertTrue(service.getHorarioDisponible(2L, desde, hasta))
            assertTrue(service.getHorarioDisponible(1L, desde, hasta).not())
        }
    }

    // ── getEventosOcupadosDelDia ──────────────────────────────────────────────

    @Nested
    inner class EventosOcupadosDelDiaTest {

        @Test
        fun `devuelve lista vacia cuando no hay eventos ese dia`() {
            whenever(eventoRepository.findAllByInicioBetweenAndEmpresa(any(), any(), any()))
                .thenReturn(emptyList())

            assertTrue(service.getEventosOcupadosDelDia(1L, LocalDateTime.now()).isEmpty())
        }

        @Test
        fun `formatea correctamente un evento que inicia y termina el mismo dia`() {
            val inicio = LocalDateTime.of(2025, 9, 10, 18, 0)
            val fin    = LocalDateTime.of(2025, 9, 10, 23, 0)
            val dto    = buildEventoDTO(nombre = "boda").also { it.inicio = inicio; it.fin = fin }

            whenever(eventoRepository.findAllByInicioBetweenAndEmpresa(any(), any(), any()))
                .thenReturn(listOf(dto))

            val result = service.getEventosOcupadosDelDia(1L, inicio)
            assertEquals(1, result.size)
            assertTrue(result[0].contains("18:00"))
            assertTrue(result[0].contains("23:00"))
            assertFalse(result[0].contains("del dia"))
        }

        @Test
        fun `formatea correctamente un evento que termina al dia siguiente`() {
            val inicio = LocalDateTime.of(2025, 9, 10, 22, 0)
            val fin    = LocalDateTime.of(2025, 9, 11,  3, 0)
            val dto    = buildEventoDTO(nombre = "after").also { it.inicio = inicio; it.fin = fin }

            whenever(eventoRepository.findAllByInicioBetweenAndEmpresa(any(), any(), any()))
                .thenReturn(listOf(dto))

            val result = service.getEventosOcupadosDelDia(1L, inicio)
            assertTrue(result[0].contains("del dia"))
            assertTrue(result[0].contains("2025-09-11"))
        }

        @Test
        fun `devuelve multiples eventos ordenados cronologicamente`() {
            val base   = LocalDateTime.of(2025, 9, 10, 0, 0)
            val dto1   = buildEventoDTO(id = 1L, nombre = "primero").also {
                it.inicio = base.withHour(10); it.fin = base.withHour(12)
            }
            val dto2   = buildEventoDTO(id = 2L, nombre = "segundo").also {
                it.inicio = base.withHour(16); it.fin = base.withHour(20)
            }

            // El repo devuelve en orden invertido para validar que el service ordena
            whenever(eventoRepository.findAllByInicioBetweenAndEmpresa(any(), any(), any()))
                .thenReturn(listOf(dto2, dto1))

            val result = service.getEventosOcupadosDelDia(1L, base)
            assertEquals(2, result.size)
            assertTrue(result[0].contains("10:00"))
            assertTrue(result[1].contains("16:00"))
        }

        @Test
        fun `el nombre del evento aparece en el string formateado`() {
            val inicio = LocalDateTime.of(2025, 9, 10, 18, 0)
            val dto    = buildEventoDTO(nombre = "cumpleanos").also {
                it.inicio = inicio; it.fin = inicio.plusHours(3)
            }

            whenever(eventoRepository.findAllByInicioBetweenAndEmpresa(any(), any(), any()))
                .thenReturn(listOf(dto))

            assertTrue(service.getEventosOcupadosDelDia(1L, inicio)[0].contains("cumpleanos"))
        }
    }

    // ── getAllEstado ──────────────────────────────────────────────────────────

    @Nested
    inner class AllEstadoTest {

        @Test
        fun `getAllEstado devuelve todos los estados`() {
            val estados = service.getAllEstado()
            assertTrue(estados.contains("RESERVADO"))
            assertTrue(estados.contains("COTIZADO"))
        }

        @Test
        fun `getAllEstadoForSaveEvento devuelve solo COTIZADO y RESERVADO`() {
            val estados = service.getAllEstadoForSaveEvento()
            assertEquals(2, estados.size)
            assertTrue(estados.contains("COTIZADO"))
            assertTrue(estados.contains("RESERVADO"))
        }

        @Test
        fun `getAllEstadoForSaveEvento NO contiene estados de gestion interna`() {
            val estados = service.getAllEstadoForSaveEvento()
            // Verificamos que no se cuelan estados que no corresponden al alta
            assertFalse(estados.contains("CANCELADO"))
            assertFalse(estados.contains("FINALIZADO"))
        }
    }

    // ── delete (soft delete) ──────────────────────────────────────────────────

    @Nested
    inner class DeleteTest {

        @Test
        fun `delete hace soft-delete seteando fechaBaja`() {
            val evento = buildEvento()
            whenever(eventoRepository.findById(1L)).thenReturn(Optional.of(evento))
            whenever(eventoRepository.save(any<Evento>())).thenAnswer { it.arguments[0] as Evento }

            service.delete(1L)

            assertNotNull(evento.fechaBaja)
            verify(eventoRepository).save(evento)
        }

        @Test
        fun `delete lanza excepcion si el evento no existe`() {
            whenever(eventoRepository.findById(99L)).thenReturn(Optional.empty())
            assertThrows(NotFoundException::class.java) { service.delete(99L) }
        }

        @Test
        fun `delete no llama a save si el evento no existe`() {
            whenever(eventoRepository.findById(99L)).thenReturn(Optional.empty())
            runCatching { service.delete(99L) }
            verify(eventoRepository, never()).save(any())
        }
    }

    @Test
    fun `getAllEventosByFecha consulta el rango completo del dia`() {
        val fecha = LocalDateTime.of(2026, 9, 25, 0, 0)
        val esperado = listOf(
            EventoDTO(1L, "fiesta", "ABCD", fecha.plusHours(18), fecha.plusHours(23), "Fiesta")
        )
        whenever(eventoRepository.getAllEventosForAgendaByFecha(
            LocalDateTime.of(2026, 9, 25, 0, 0),
            LocalDateTime.of(2026, 9, 25, 23, 59, 59),
            3L
        )).thenReturn(esperado)

        assertEquals(esperado, service.getAllEventosByFecha("2026-09-25", 3L))
    }

    @Test
    fun `getAllEventosByFecha rechaza una fecha con formato invalido`() {
        assertThrows(DateTimeParseException::class.java) { service.getAllEventosByFecha("25-09-2026", 3L) }
        verifyNoInteractions(eventoRepository)
    }

    @Test
    fun `listados paginados y cantidades delegan al repositorio`() {
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(buildEventoDTO()))
        whenever(eventoRepository.eventosByEmpresa(3L, pageable)).thenReturn(page)
        whenever(eventoRepository.eventosByNombre(3L, "boda", pageable)).thenReturn(page)
        whenever(eventoRepository.cantidadDeEventos(3L)).thenReturn(8)
        whenever(eventoRepository.cantidadDeEventosFiltrados(3L, "boda")).thenReturn(2)

        assertEquals(page, service.getAllEventoByEmpresaId(3L, pageable))
        assertEquals(page, service.getAllEventoByFilterName(3L, "boda", pageable))
        assertEquals(8, service.cantEventos(3L))
        assertEquals(2, service.cantEventosFiltrados(3L, "boda"))
    }

    @Test
    fun `consultas por usuario delegan al repositorio`() {
        val eventos = listOf(EventoConUsuarioDTO(1L, "boda", "ABCD", 4L, "Ana", "Perez", null))
        whenever(eventoRepository.getEventosByUsuarioIdAndEmpresaId(4L, 3L)).thenReturn(eventos)
        whenever(eventoRepository.getCantEventosByUsuarioIdAndEmpresaId(4L, 3L)).thenReturn(1)

        assertEquals(eventos, service.getEventosByUsuarioAndEmpresa(4L, 3L))
        assertEquals(1, service.getCantEventosByUsuarioAndEmpresa(4L, 3L))
    }

    @Test
    fun `getPresupuesto devuelve el presupuesto calculado del evento`() {
        val evento = mock<Evento>()
        whenever(eventoRepository.findById(1L)).thenReturn(Optional.of(evento))
        whenever(evento.getPresupuestoTotal()).thenReturn(1250.0)

        assertEquals(1250.0, service.getPresupuesto(1L))
    }

    @Test
    fun `getEventoHora devuelve los datos horarios del evento`() {
        val evento = buildEvento()
        whenever(eventoRepository.findById(1L)).thenReturn(Optional.of(evento))

        val horario = service.getEventoHora(1L)

        assertEquals(evento.id, horario?.id)
        assertEquals(evento.inicio, horario?.inicio)
        assertEquals(evento.fin, horario?.fin)
    }

    @Test
    fun `getEventoExtra filtra extras de evento y variables`() {
        val evento = buildEvento()
        whenever(eventoRepository.findById(1L)).thenReturn(Optional.of(evento))
        whenever(extraService.fromListaExtraToListaExtraDtoByFilter(evento.empresa, evento.listaExtra, evento.inicio, TipoExtra.EVENTO)).thenReturn(emptyList())
        whenever(extraVariableService.fromListaExtraVariableToListaExtraVariableDtoByFilter(evento.empresa, evento.listaEventoExtraVariable, evento.inicio, TipoExtra.VARIABLE_EVENTO)).thenReturn(emptyList())

        val result = service.getEventoExtra(1L)

        assertEquals(evento.id, result?.id)
        verify(extraService).fromListaExtraToListaExtraDtoByFilter(evento.empresa, evento.listaExtra, evento.inicio, TipoExtra.EVENTO)
        verify(extraVariableService).fromListaExtraVariableToListaExtraVariableDtoByFilter(evento.empresa, evento.listaEventoExtraVariable, evento.inicio, TipoExtra.VARIABLE_EVENTO)
    }

    @Test
    fun `getEventoCatering filtra extras de catering y variables`() {
        val evento = buildEvento()
        whenever(eventoRepository.findById(1L)).thenReturn(Optional.of(evento))
        whenever(extraService.fromListaExtraToListaExtraDtoByFilter(evento.empresa, evento.listaExtra, evento.inicio, TipoExtra.TIPO_CATERING)).thenReturn(emptyList())
        whenever(extraVariableService.fromListaExtraVariableToListaExtraVariableDtoByFilter(evento.empresa, evento.listaEventoExtraVariable, evento.inicio, TipoExtra.VARIABLE_CATERING)).thenReturn(emptyList())

        val result = service.getEventoCatering(1L)

        assertEquals(evento.id, result?.id)
        verify(extraService).fromListaExtraToListaExtraDtoByFilter(evento.empresa, evento.listaExtra, evento.inicio, TipoExtra.TIPO_CATERING)
        verify(extraVariableService).fromListaExtraVariableToListaExtraVariableDtoByFilter(evento.empresa, evento.listaEventoExtraVariable, evento.inicio, TipoExtra.VARIABLE_CATERING)
    }

    @Test
    fun `getEventoVer solicita las cuatro categorias de extras`() {
        val evento = buildEvento()
        whenever(eventoRepository.findById(1L)).thenReturn(Optional.of(evento))
        whenever(extraService.fromListaExtraToListaExtraDtoByFilter(any(), any(), any(), any())).thenReturn(emptyList())
        whenever(extraVariableService.fromListaExtraVariableToListaExtraVariableDtoByFilter(any(), any(), any(), any())).thenReturn(emptyList())

        service.getEventoVer(1L)

        verify(extraService).fromListaExtraToListaExtraDtoByFilter(evento.empresa, evento.listaExtra, evento.inicio, TipoExtra.EVENTO)
        verify(extraService).fromListaExtraToListaExtraDtoByFilter(evento.empresa, evento.listaExtra, evento.inicio, TipoExtra.TIPO_CATERING)
        verify(extraVariableService).fromListaExtraVariableToListaExtraVariableDtoByFilter(evento.empresa, evento.listaEventoExtraVariable, evento.inicio, TipoExtra.VARIABLE_EVENTO)
        verify(extraVariableService).fromListaExtraVariableToListaExtraVariableDtoByFilter(evento.empresa, evento.listaEventoExtraVariable, evento.inicio, TipoExtra.VARIABLE_CATERING)
    }

    @Test
    fun `editEventoExtra conserva extras de catering y reemplaza los de evento`() {
        val evento = buildEvento()
        val catering = Extra(1L, "catering", TipoExtra.TIPO_CATERING)
        val anteriorEvento = Extra(2L, "anterior", TipoExtra.EVENTO)
        val nuevoEvento = Extra(3L, "nuevo", TipoExtra.EVENTO)
        evento.listaExtra.addAll(listOf(catering, anteriorEvento))
        whenever(eventoRepository.findById(1L)).thenReturn(Optional.of(evento))
        whenever(extraService.fromListaExtraDtoToListaExtra(emptyList())).thenReturn(listOf(nuevoEvento))
        whenever(extraVariableService.fromListaExtraVariableDtoToListaExtraVariable(emptyList())).thenReturn(emptyList())
        whenever(eventoRepository.save(any<Evento>())).thenAnswer { it.arguments[0] as Evento }
        val dto = EventoExtraDTO(1L, "evento", "ABCD", 55.0, 10L, emptyList(), emptyList(), mock(), evento.inicio)

        assertEquals(1L, service.editEventoExtra(dto))

        assertEquals(setOf(catering, nuevoEvento), evento.listaExtra)
        assertEquals(55.0, evento.extraOtro)
        assertEquals(10L, evento.descuento)
    }

    @Test
    fun `editEventoCatering conserva extras de evento y reemplaza los de catering`() {
        val evento = buildEvento()
        val normal = Extra(1L, "evento", TipoExtra.EVENTO)
        val anteriorCatering = Extra(2L, "anterior", TipoExtra.TIPO_CATERING)
        val nuevoCatering = Extra(3L, "nuevo", TipoExtra.TIPO_CATERING)
        evento.listaExtra.addAll(listOf(normal, anteriorCatering))
        whenever(eventoRepository.findById(1L)).thenReturn(Optional.of(evento))
        whenever(extraService.fromListaExtraDtoToListaExtra(emptyList())).thenReturn(listOf(nuevoCatering))
        whenever(extraVariableService.fromListaExtraVariableDtoToListaExtraVariable(emptyList())).thenReturn(emptyList())
        whenever(eventoRepository.save(any<Evento>())).thenAnswer { it.arguments[0] as Evento }
        val dto = EventoCateringDTO(1L, "evento", "ABCD", 75.0, "menu", emptyList(), emptyList(), 2L, evento.inicio, 100, 20)

        assertEquals(1L, service.editEventoCatering(dto))

        assertEquals(setOf(normal, nuevoCatering), evento.listaExtra)
        assertEquals(75.0, evento.cateringOtro)
        assertEquals("menu", evento.cateringOtroDescripcion)
    }

    @Test
    fun `editEventoHora actualiza y persiste las fechas`() {
        val evento = buildEvento()
        val inicio = LocalDateTime.of(2026, 10, 5, 18, 0)
        val fin = inicio.plusHours(4)
        whenever(eventoRepository.findById(1L)).thenReturn(Optional.of(evento))
        whenever(eventoRepository.save(any<Evento>())).thenAnswer { it.arguments[0] as Evento }

        val result = service.editEventoHora(EventoHoraDTO(1L, "evento", "ABCD", inicio, fin))

        assertEquals(inicio, evento.inicio)
        assertEquals(fin, evento.fin)
        assertEquals(inicio, result.inicio)
        verify(eventoRepository).save(evento)
    }

    @Test
    fun `editEventoCapacidad actualiza y devuelve las capacidades`() {
        val evento = buildEvento()
        whenever(eventoRepository.findById(1L)).thenReturn(Optional.of(evento))
        whenever(eventoRepository.save(any<Evento>())).thenAnswer { it.arguments[0] as Evento }

        val capacidad = service.editEventoCapacidad(1L, EventoCapacidadDTO(80, 15))

        assertEquals(80, capacidad.capacidadAdultos)
        assertEquals(15, capacidad.capacidadNinos)
        assertEquals(80, evento.capacidadAdultos)
        verify(eventoRepository).save(evento)
    }

    @Test
    fun `editEventoNombre y anotaciones guardan los cambios`() {
        val evento = buildEvento()
        whenever(eventoRepository.findById(1L)).thenReturn(Optional.of(evento))
        whenever(eventoRepository.save(any<Evento>())).thenAnswer { it.arguments[0] as Evento }

        assertEquals("nuevo nombre", service.editEventoNombre("nuevo nombre", 1L))
        assertEquals("nueva nota", service.editEventoAnotaciones("nueva nota", 1L))
        assertEquals("nuevo nombre", evento.nombre)
        assertEquals("nueva nota", evento.anotaciones)
        verify(eventoRepository, times(2)).save(evento)
    }

    @Test
    fun `generarEstadoDeCuentaPDF delega al servicio PDF`() {
        val evento = buildEvento()
        val pdf = byteArrayOf(4, 5)
        whenever(eventoRepository.findById(1L)).thenReturn(Optional.of(evento))
        whenever(pdfService.generarEstadoDeCuenta(evento)).thenReturn(pdf)

        assertArrayEquals(pdf, service.generarEstadoDeCuentaPDF(1L))
        verify(pdfService).generarEstadoDeCuenta(evento)
    }

    @Test
    fun `getAllEventosForAgendaByEmpresaId devuelve eventos del repositorio`() {
        val agenda = listOf(EventoAgendaDTO(1L, "boda", LocalDateTime.now(), LocalDateTime.now().plusHours(4)))
        whenever(eventoRepository.getAllEventosForAgendaByEmpresaId(eq(3L), any())).thenReturn(agenda)

        assertEquals(agenda, service.getAllEventosForAgendaByEmpresaId(3L))
        verify(eventoRepository).getAllEventosForAgendaByEmpresaId(eq(3L), any())
    }

    @Test
    fun `descargarEvento obtiene el evento y delega la generacion del PDF`() {
        val evento = buildEvento()
        val pdf = byteArrayOf(1, 2, 3)
        whenever(eventoRepository.findById(1L)).thenReturn(Optional.of(evento))
        whenever(pdfService.generarComprobanteEvento(evento)).thenReturn(pdf)

        assertArrayEquals(pdf, service.descargarEvento(1L))
        verify(pdfService).generarComprobanteEvento(evento)
    }

    @Test
    fun `reenviarMail envia el comprobante y devuelve true`() {
        val evento = buildEvento()
        val empresa = mock<Empresa>()
        whenever(eventoRepository.findById(1L)).thenReturn(Optional.of(evento))
        whenever(empresaService.findById(2L)).thenReturn(empresa)

        assertTrue(service.reenviarMail(1L, 2L))
        verify(emailService).enviarMailComprabanteReserva(evento, "sido reservado (reenvío)", empresa)
    }

    @Test
    fun `reenviarMail transforma errores en NotFoundException`() {
        whenever(eventoRepository.findById(99L)).thenReturn(Optional.empty())

        val error = assertThrows(NotFoundException::class.java) { service.reenviarMail(99L, 2L) }

        assertTrue(error.message!!.contains("No se pudo reenviar mail"))
        verifyNoInteractions(emailService)
    }

    @Test
    fun `reenviarMail transforma error al buscar la empresa`() {
        val evento = buildEvento()
        whenever(eventoRepository.findById(1L)).thenReturn(Optional.of(evento))
        whenever(empresaService.findById(2L)).thenThrow(IllegalStateException("empresa no disponible"))

        val error = assertThrows(NotFoundException::class.java) { service.reenviarMail(1L, 2L) }

        assertTrue(error.message!!.contains("empresa no disponible"))
        verifyNoInteractions(emailService)
    }
}
