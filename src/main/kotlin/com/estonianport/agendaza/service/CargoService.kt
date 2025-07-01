package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dto.AgendaDTO
import com.estonianport.agendaza.repository.CargoRepository
import com.estonianport.agendaza.model.Cargo
import com.estonianport.agendaza.model.enums.TipoCargo
import com.estonianport.agendaza.model.Usuario
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

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

    fun getListaCargosByUsuarioId(usuarioId : Long) : List<AgendaDTO> {
        return cargoRepository.getListaCargosByUsuarioId(usuarioId)
    }

    fun getCargoByEmpresaIdAndUsuarioId(empresaId : Long, usuarioId : Long) : Cargo {
        return cargoRepository.getCargoByEmpresaIdAndUsuarioId(empresaId, usuarioId)
    }

    fun getTipoCargoByEmpresaIdAndUsuarioId(empresaId : Long, usuarioId : Long) : TipoCargo {
        return cargoRepository.getTipoCargoByEmpresaIdAndUsuarioId(empresaId, usuarioId)
    }

    fun delete(empresaId: Long, usuarioId: Long) {
        val cargo : Cargo = cargoRepository.getCargoByEmpresaIdAndUsuarioId(empresaId, usuarioId)
        cargo.fechaBaja = LocalDate.now()
        cargoRepository.save(cargo)
    }
}