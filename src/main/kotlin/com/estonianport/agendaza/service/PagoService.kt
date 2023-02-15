package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dao.PagoDao
import com.estonianport.agendaza.dao.PrecioConFechaDao
import com.estonianport.agendaza.dto.PagoDto
import com.estonianport.agendaza.errors.BusinessException
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Pago
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
class PagoService : GenericServiceImpl<Pago, Long>(){

    @Autowired
    lateinit var pagoDao: PagoDao

    override val dao: CrudRepository<Pago, Long>
        get() = pagoDao

    fun getEventoForPago(codigo : String, empresa : Empresa) : PagoDto {
        val evento = empresa.listaEvento.find { it.codigo == codigo }

        if(evento != null){
            return PagoDto(0, 0, evento.codigo, "", evento.nombre, evento.inicio)
        }
        throw NotFoundException("No se encontró el evento con codigo: ${codigo}")
    }
}