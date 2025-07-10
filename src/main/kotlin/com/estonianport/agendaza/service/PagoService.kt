package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.repository.PagoRepository
import com.estonianport.agendaza.dto.PagoDTO
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.Adelanto
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Evento
import com.estonianport.agendaza.model.enums.MedioDePago
import com.estonianport.agendaza.model.Pago
import com.estonianport.agendaza.model.Cuota
import com.estonianport.agendaza.model.Senia
import com.estonianport.agendaza.model.PagoTotal
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

    fun getEventoForPago(codigo : String, empresa : Empresa) : PagoDTO {
        val evento = empresa.listaEvento.find { it.codigo == codigo }

        if(evento != null){
            return PagoDTO(0, 0.0, Concepto.SENIA,null,evento.codigo, MedioDePago.TRANSFERENCIA, evento.nombre, evento.inicio)
        }
        throw NotFoundException("No se encontró el evento con codigo: ${codigo}")
    }

    fun fromListaPagoToListaPagoDto(listaPago : MutableSet<Pago>) : List<PagoDTO>{
        return listaPago.map{ pago -> pago.toDTO() }}

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
        return pagoRepository.getAllPagoFromEvento(eventoId)
    }

    fun fromDTO(pagoDTO: PagoDTO, evento: Evento, encargado: Usuario): Pago {
        return Pago.build(pagoDTO.id,pagoDTO.monto, pagoDTO.concepto,
            pagoDTO.medioDePago, LocalDateTime.now(), evento, encargado,
            pagoDTO.numeroCuota)
    }


}