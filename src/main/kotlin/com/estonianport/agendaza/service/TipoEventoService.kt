package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dao.TipoEventoDao
import com.estonianport.agendaza.dto.ExtraDto
import com.estonianport.agendaza.model.PrecioConFechaTipoEvento
import com.estonianport.agendaza.model.TipoEvento
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TipoEventoService : GenericServiceImpl<TipoEvento, Long>() {

    @Autowired
    lateinit var tipoEventoDao: TipoEventoDao

    override val dao: CrudRepository<TipoEvento, Long>
        get() = tipoEventoDao

    fun getPrecioByFecha(listaPrecioConFechaTipoEvento: MutableSet<PrecioConFechaTipoEvento>, fechaEvento : LocalDateTime): Int{

        if(listaPrecioConFechaTipoEvento.isNotEmpty() && listaPrecioConFechaTipoEvento.find { it.desde == fechaEvento || it.desde.isBefore(fechaEvento) && it.hasta.isAfter(fechaEvento) } != null ){
            return listaPrecioConFechaTipoEvento.find { it.desde == fechaEvento || it.desde.isBefore(fechaEvento) && it.hasta.isAfter(fechaEvento) }!!.precio
        }else{
            return 0
        }
    }

}