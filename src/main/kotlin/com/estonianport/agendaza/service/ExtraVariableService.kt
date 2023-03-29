package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dao.ExtraVariableDao
import com.estonianport.agendaza.model.EventoExtraVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service


@Service
class ExtraVariableService : GenericServiceImpl<EventoExtraVariable, Long>() {

    @Autowired
    lateinit var extraVariableDao: ExtraVariableDao

    override val dao: CrudRepository<EventoExtraVariable, Long>
        get() = extraVariableDao

}
