package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dao.PersonaDao
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.Persona
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
class PersonaService : GenericServiceImpl<Persona, Long>(){

    @Autowired
    lateinit var personaDao: PersonaDao

    override val dao: CrudRepository<Persona, Long>
        get() = personaDao
}