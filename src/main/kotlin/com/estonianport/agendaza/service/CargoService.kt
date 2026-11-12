package com.estonianport.agendaza.service

import com.estonianport.agendaza.common.GenericServiceImpl
import com.estonianport.agendaza.dto.AgendaDTO
import com.estonianport.agendaza.repository.CargoRepository
import com.estonianport.agendaza.model.Cargo
import com.estonianport.agendaza.errors.NotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class CargoService(
    val cargoRepository: CargoRepository
) : GenericServiceImpl<Cargo, Long>() {

    override val dao: CrudRepository<Cargo, Long>
        get() = cargoRepository

    fun findById(id : Long) : Cargo{
        return cargoRepository.findById(id).orElseThrow {
            NotFoundException("Cargo no encontrado con id: $id")
        }
    }

    fun getListaCargosByUsuarioId(usuarioId : Long) : List<AgendaDTO> {
        return cargoRepository.getListaCargosByUsuarioId(usuarioId)
    }

    fun getCargoByEmpresaIdAndUsuarioId(empresaId : Long, usuarioId : Long) : Cargo {
        return cargoRepository.getCargoByEmpresaIdAndUsuarioId(empresaId, usuarioId)
    }

    fun delete(empresaId: Long, usuarioId: Long) {
        val cargo = cargoRepository.getCargoByEmpresaIdAndUsuarioId(empresaId, usuarioId)
        cargo.fechaBaja = LocalDate.now()
        cargoRepository.save(cargo)
    }

    fun esAdminDeUsuario(username: String, usuarioId: Long): Boolean =
        cargoRepository.esAdminDeUsuarioEnEmpresa(username, usuarioId)

    fun getPrimerCargoActivo(usuarioId: Long): Cargo? =
        cargoRepository.findFirstByUsuario_IdAndFechaBajaIsNull(usuarioId)

    fun getCargoDeUsuarioParaAdmin(username: String, usuarioId: Long): Cargo? =
        cargoRepository.findCargoDeUsuarioParaAdmin(username, usuarioId)
}
