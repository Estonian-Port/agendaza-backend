package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dto.AgendaDto
import com.estonianport.agendaza.repository.CargoRepository
import com.estonianport.agendaza.model.Cargo
import com.estonianport.agendaza.model.Usuario
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
class CargoService : GenericServiceImpl<Cargo, Long>() {

    @Autowired
    lateinit var cargoRepository: CargoRepository

    override val dao: CrudRepository<Cargo, Long>
        get() = cargoRepository

    fun findAllByUsuario(usuario : Usuario) : List<Cargo>{
        return cargoRepository.findAllByUsuario(usuario)
    }

    fun findById(id : Long) : Cargo{
        return cargoRepository.findById(id).get()
    }

    fun getListaCargosByUsuarioId(usuarioId : Long) : List<AgendaDto> {
        return cargoRepository.getListaCargosByUsuarioId(usuarioId)
    }
}