package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.common.emailService.EmailService
import com.estonianport.agendaza.common.openPDF.PdfService
import com.estonianport.agendaza.dto.EventoPagoDTO
import com.estonianport.agendaza.dto.PagoDTO
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.Pago
import com.estonianport.agendaza.repository.PagoRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class PagoService(
    private val pagoRepository: PagoRepository,
    private val eventoService: EventoService,
    private val usuarioService: UsuarioService,
    private val empresaService: EmpresaService,
    private val pdfService: PdfService,
    private val emailService: EmailService
) : GenericServiceImpl<Pago, Long>() {

    override val dao: CrudRepository<Pago, Long>
        get() = pagoRepository

    // ==================== QUERIES ====================

    @Transactional(readOnly = true)
    fun getPagoDTO(id: Long): PagoDTO {
        return pagoRepository.findById(id)
            .orElseThrow { NotFoundException("Pago no encontrado con id: $id") }
            .toDTO()
    }

    @Transactional(readOnly = true)
    fun getEventoForEditEventoPago(eventoId: Long): EventoPagoDTO {
        val evento = eventoService.findById(eventoId)
        val eventoPagoDto = pagoRepository.getEventoForPago(eventoId)
            ?: throw NotFoundException("No se encontró el evento con id: $eventoId")

        eventoPagoDto.precioTotal = evento.getPresupuestoTotal()
        return eventoPagoDto
    }

    @Transactional(readOnly = true)
    fun getEventoForSavePago(eventoId: Long): PagoDTO {
        return pagoRepository.getEventoForSavePago(eventoId, LocalDateTime.now())
            ?: throw NotFoundException("No se encontró el evento con id: $eventoId")
    }

    @Transactional(readOnly = true)
    fun contadorDePagos(empresaId: Long): Int = pagoRepository.cantidadPagos(empresaId)

    @Transactional(readOnly = true)
    fun pagos(empresaId: Long, pageNumber: Int): List<PagoDTO> {
        return pagoRepository.findAll(empresaId, PageRequest.of(pageNumber, 10))
            .content
            .map { it.toDTO() }
    }

    @Transactional(readOnly = true)
    fun pagosFiltrados(empresaId: Long, pageNumber: Int, buscar: String): List<PagoDTO> {
        return pagoRepository.pagosByNombre(empresaId, buscar, PageRequest.of(pageNumber, 10))
            .content
            .map { it.toDTO() }
    }

    @Transactional(readOnly = true)
    fun contadorDePagosFiltrados(empresaId: Long, buscar: String): Int {
        return pagoRepository.cantidadPagosFiltrados(empresaId, buscar)
    }

    @Transactional(readOnly = true)
    fun getAllPagoFromEvento(eventoId: Long): List<PagoDTO> {
        return pagoRepository.getAllPagoFromEvento(eventoId)
            ?: throw NotFoundException("No hay pagos registrados para el evento con id: $eventoId")
    }

    // ==================== MUTATIONS ====================

    @Transactional
    fun savePago(pagoDTO: PagoDTO): PagoDTO {
        val evento = eventoService.getByCodigoAndEmpresaId(pagoDTO.codigo, pagoDTO.empresaId)
        val encargado = usuarioService.get(pagoDTO.usuarioId)
            ?: throw NotFoundException("Usuario no encontrado con id: ${pagoDTO.usuarioId}")

        val fecha = if (pagoDTO.fecha.toLocalDate() != LocalDate.now()) pagoDTO.fecha else LocalDateTime.now()

        val pago = Pago(
            pagoDTO.id,
            pagoDTO.monto,
            pagoDTO.concepto ?: throw IllegalArgumentException("El concepto no puede ser nulo"),
            pagoDTO.medioDePago ?: throw IllegalArgumentException("El medio de pago no puede ser nulo"),
            fecha,
            evento,
            encargado,
            pagoDTO.numeroCuota
        )

        return pagoRepository.save(pago).toDTO()
    }

    @Transactional
    override fun delete(id: Long) {
        val pago = pagoRepository.findById(id)
            .orElseThrow { NotFoundException("Pago no encontrado con id: $id") }

        pago.fechaBaja = LocalDate.now()
        pagoRepository.save(pago)
    }

    // ==================== PDF Y EMAIL ====================

    @Transactional(readOnly = true)
    fun generarComprobantePago(pagoId: Long): ByteArray {
        val pago = pagoRepository.findById(pagoId)
            .orElseThrow { NotFoundException("Pago no encontrado con id: $pagoId") }
        return pdfService.generarComprobanteDePago(pago)
    }

    @Transactional(readOnly = true)
    fun generarEstadoCuenta(eventoId: Long): ByteArray {
        val evento = eventoService.findById(eventoId)
        return pdfService.generarEstadoDeCuenta(evento)
    }

    @Transactional(readOnly = true)
    fun enviarEmailPago(pagoId: Long, eventoId: Long, empresaId: Long): Boolean {
        val pago = pagoRepository.findById(pagoId)
            .orElseThrow { NotFoundException("Pago no encontrado con id: $pagoId") }
        val evento = eventoService.findById(eventoId)
        val empresa = empresaService.get(empresaId)
            ?: throw NotFoundException("Empresa no encontrada con id: $empresaId")

        emailService.enviarEmailPago(pago, evento, empresa)
        return true
    }

    @Transactional(readOnly = true)
    fun enviarEmailEstadoCuenta(eventoId: Long, empresaId: Long): Boolean {
        val evento = eventoService.findById(eventoId)
        val empresa = empresaService.get(empresaId)
            ?: throw NotFoundException("Empresa no encontrada con id: $empresaId")

        emailService.enviarEmailEstadoCuenta(evento, empresa)
        return true
    }
}