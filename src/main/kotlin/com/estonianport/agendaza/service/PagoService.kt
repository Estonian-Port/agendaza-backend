package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.repository.PagoRepository
import com.estonianport.agendaza.dto.PagoDto
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.MedioDePago
import com.estonianport.agendaza.model.Pago
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
class PagoService : GenericServiceImpl<Pago, Long>(){

    @Autowired
    lateinit var pagoRepository: PagoRepository

    override val dao: CrudRepository<Pago, Long>
        get() = pagoRepository

    fun getEventoForPago(codigo : String, empresa : Empresa) : PagoDto {
        val evento = empresa.listaEvento.find { it.codigo == codigo }

        if(evento != null){
            return PagoDto(0, 0, evento.codigo, MedioDePago.TRANSFERENCIA, evento.nombre, evento.inicio)
        }
        throw NotFoundException("No se encontró el evento con codigo: ${codigo}")
    }

    fun fromListaPagoToListaPagoDto(listaPago : MutableSet<Pago>) : List<PagoDto>{
        return listaPago.map{ pago -> pago.toDTO() }
    }
}