package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.model.Salon
import com.estonianport.agendaza.repository.SalonDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
class SalonService : GenericServiceImpl<Salon, Long>() {

    @Autowired
    lateinit var salonDao: SalonDao

    override val dao: CrudRepository<Salon, Long>
        get() = salonDao

    fun count(): Long? {
        return salonDao.count()
    }
}