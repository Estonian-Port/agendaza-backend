package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dao.CargoDao
import com.estonianport.agendaza.model.Cargo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
class CargoService : GenericServiceImpl<Cargo, Long>() {

    @Autowired
    lateinit var cargoDao: CargoDao

    override val dao: CrudRepository<Cargo, Long>
        get() = cargoDao
}