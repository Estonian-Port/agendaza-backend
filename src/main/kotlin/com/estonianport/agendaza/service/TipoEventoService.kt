package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dao.TipoEventoDao
import com.estonianport.agendaza.model.TipoEvento
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
class TipoEventoService : GenericServiceImpl<TipoEvento, Long>() {

    @Autowired
    lateinit var tipoEventoDao: TipoEventoDao

    override val dao: CrudRepository<TipoEvento, Long>
        get() = tipoEventoDao

    fun count(): Long? {
        return tipoEventoDao.count()
    }
}