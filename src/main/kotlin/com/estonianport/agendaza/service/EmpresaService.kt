package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.dao.EmpresaDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
class EmpresaService : GenericServiceImpl<Empresa, Long>() {

    @Autowired
    lateinit var EmpresaDao: EmpresaDao

    override val dao: CrudRepository<Empresa, Long>
        get() = EmpresaDao

}