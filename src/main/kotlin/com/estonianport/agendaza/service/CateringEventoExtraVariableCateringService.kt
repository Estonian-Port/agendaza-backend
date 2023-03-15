package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dao.CateringEventoExtraVariableCateringDao
import com.estonianport.agendaza.model.CateringEventoExtraVariableCatering
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service


@Service
class CateringEventoExtraVariableCateringService : GenericServiceImpl<CateringEventoExtraVariableCatering, Long>() {

    @Autowired
    lateinit var cateringEventoExtraVariableCateringDao: CateringEventoExtraVariableCateringDao

    override val dao: CrudRepository<CateringEventoExtraVariableCatering, Long>
        get() = cateringEventoExtraVariableCateringDao

}
