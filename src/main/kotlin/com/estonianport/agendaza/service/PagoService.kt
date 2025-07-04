package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.repository.PagoRepository
import com.estonianport.agendaza.dto.PagoDTO
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.MedioDePago
import com.estonianport.agendaza.model.Pago
import org.apache.juli.logging.Log
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class PagoService : GenericServiceImpl<Pago, Long>(){

    @Autowired
    lateinit var pagoRepository: PagoRepository

    override val dao: CrudRepository<Pago, Long>
        get() = pagoRepository

    fun getEventoForPago(codigo : String, empresa : Empresa) : PagoDTO {
        val evento = empresa.listaEvento.find { it.codigo == codigo }

        if(evento != null){
            return PagoDTO(0, 0.0, evento.codigo, MedioDePago.TRANSFERENCIA, evento.nombre, evento.inicio)
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

    fun getAllPagoFromEvento(idEvento: Long): List<PagoDTO> {
        return pagoRepository.getAllPagoFromEvento(idEvento)
    }
}