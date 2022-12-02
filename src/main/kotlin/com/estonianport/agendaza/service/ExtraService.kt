package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dao.ExtraDao
import com.estonianport.agendaza.model.Extra
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
class ExtraService : GenericServiceImpl<Extra, Long>(){

    @Autowired
    lateinit var extraDao: ExtraDao

    override val dao: CrudRepository<Extra, Long>
        get() = extraDao

}