package com.estonianport.agendaza.service.impl

import GenericServiceImpl
import com.estonianport.agendaza.model.Salon
import com.estonianport.agendaza.repository.SalonDao
import com.estonianport.agendaza.service.SalonService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
class SalonServiceImpl : GenericServiceImpl<Salon, Long>(), SalonService {

    @Autowired
    lateinit var salonDao: SalonDao

    override val dao: CrudRepository<Salon, Long>
        get() = salonDao

    override fun count(): Long? {
        return salonDao.count()
    }
}