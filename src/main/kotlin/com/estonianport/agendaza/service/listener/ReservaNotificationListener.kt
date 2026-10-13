package com.estonianport.agendaza.service.listener

import ReservaRegistradaEvent
import com.estonianport.agendaza.common.emailService.EmailService
import com.estonianport.agendaza.repository.EventoRepository
import com.estonianport.agendaza.service.EmpresaService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ReservaNotificationListener(
    private val eventoRepository: EventoRepository,
    private val empresaService: EmpresaService,
    private val emailService: EmailService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    fun onReservaRegistrada(e: ReservaRegistradaEvent) {
        try {
            val evento = eventoRepository.findById(e.eventoId).orElse(null) ?: return
            val empresa = empresaService.findById(e.empresaId)
            if (emailService.isEmailValid(evento.cliente.email)) {
                emailService.enviarMailComprabanteReserva(evento, "sido reservado", empresa)
            }
        } catch (ex: Exception) {
            logger.error("Fallo mail de reserva ${e.eventoId}: ${ex.message}", ex)
        }
    }
}