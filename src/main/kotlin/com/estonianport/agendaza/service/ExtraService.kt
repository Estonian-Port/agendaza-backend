package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dao.ExtraDao
import com.estonianport.agendaza.dto.ExtraDto
import com.estonianport.agendaza.model.Extra
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ExtraService : GenericServiceImpl<Extra, Long>(){

    @Autowired
    lateinit var extraDao: ExtraDao

    override val dao: CrudRepository<Extra, Long>
        get() = extraDao

    fun getListaExtraDto(listaExtra: MutableSet<Extra>, fechaEvento : LocalDateTime): MutableSet<ExtraDto>{
        val listaExtraDto = mutableSetOf<ExtraDto>()

        listaExtra.forEach{
            val extraDto = ExtraDto(it.id, it.nombre, it.tipoExtra, it.empresa.id)
            if(it.listaPrecioConFecha.isNotEmpty() && it.listaPrecioConFecha.find { it.desde == fechaEvento || it.desde.isBefore(fechaEvento) && it.hasta.isAfter(fechaEvento) } != null ){
                extraDto.precio = it.listaPrecioConFecha.find { it.desde == fechaEvento || it.desde.isBefore(fechaEvento) && it.hasta.isAfter(fechaEvento) }!!.precio
            }else{
                extraDto.precio = 0
            }
            listaExtraDto.add(extraDto)
        }
        return listaExtraDto
    }
}