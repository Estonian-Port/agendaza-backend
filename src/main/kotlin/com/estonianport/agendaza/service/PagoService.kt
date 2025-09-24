package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dto.EventoPagoDTO
import com.estonianport.agendaza.repository.PagoRepository
import com.estonianport.agendaza.dto.PagoDTO
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Evento
import com.estonianport.agendaza.model.enums.MedioDePago
import com.estonianport.agendaza.model.Pago
import com.estonianport.agendaza.model.Usuario
import com.estonianport.agendaza.model.enums.Concepto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class PagoService : GenericServiceImpl<Pago, Long>(){

    @Autowired
    lateinit var pagoRepository: PagoRepository

    override val dao: CrudRepository<Pago, Long>
        get() = pagoRepository

    fun getEventoForEditEventoPago(evento: Evento) : EventoPagoDTO {
        val eventoPagoDto = pagoRepository.getEventoForPago(evento.id)?:
            throw NotFoundException("No se encontró el evento")

        eventoPagoDto.precioTotal = evento.getPresupuestoTotal()

        return eventoPagoDto
    }

    fun getEventoForSavePago(eventoId : Long): PagoDTO {
        return pagoRepository.getEventoForSavePago(eventoId, LocalDateTime.now())?:
            throw NotFoundException("No se encontró el evento")
    }

    fun contadorDePagos(id : Long) = pagoRepository.cantidadPagos(id)

    fun pagos(id: Long, pageNumber : Int) = pagoRepository.findAll(id, PageRequest.of(pageNumber,10)).content
        .map { pago -> pago.toDTO()}

    fun pagosFiltrados(id : Long, pageNumber : Int, buscar: String)=
        pagoRepository.pagosByNombre(id, buscar, PageRequest.of(pageNumber,10)).content
            .map { pago -> pago.toDTO()}

    fun contadorDePagosFiltrados(id : Long,buscar : String) =
            pagoRepository.cantidadPagosFiltrados(id,buscar)


    override fun delete(id: Long) {
        val pago : Pago = pagoRepository.findById(id)
                .orElseThrow { NotFoundException("Pago no encontrado") }
        pago.fechaBaja = LocalDate.now()
        pagoRepository.save(pago)
    }

    fun getAllPagoFromEvento(eventoId: Long): List<PagoDTO> {
        return pagoRepository.getAllPagoFromEvento(eventoId)?:
            throw NotFoundException("No hay pagos registrados para el evento")
    }

    fun fromDTO(pagoDTO: PagoDTO, evento: Evento, encargado: Usuario): Pago {
        val fecha = if(pagoDTO.fecha.toLocalDate() != LocalDate.now()) pagoDTO.fecha else LocalDateTime.now()

        return Pago(pagoDTO.id,pagoDTO.monto, pagoDTO.concepto!!,
            pagoDTO.medioDePago!!, fecha, evento, encargado,
            pagoDTO.numeroCuota)
    }

}