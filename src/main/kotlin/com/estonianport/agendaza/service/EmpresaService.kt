package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.repository.EmpresaRepository
import com.estonianport.agendaza.dto.EventoDto
import com.estonianport.agendaza.dto.PagoDto
import com.estonianport.agendaza.dto.UsuarioAbmDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
class EmpresaService : GenericServiceImpl<Empresa, Long>() {

    @Autowired
    lateinit var empresaRepository: EmpresaRepository

    override val dao: CrudRepository<Empresa, Long>
        get() = empresaRepository

    fun getAllEventoByEmpresaId(empresa : Empresa): List<EventoDto> {
        return empresa.listaEvento.map { evento ->
            evento.toDto()
        }
    }

    fun getAllPagoByEmpresaId(empresa : Empresa): List<PagoDto> {
        return empresa.listaEvento.flatMap { evento ->
            evento.listaPago.map { pago ->
                pago.toDTO()
            }
        }
    }

    fun getAllUsuariosByEmpresaId(empresa: Empresa): List<UsuarioAbmDto> {
        return empresa.listaEmpleados.map {
            UsuarioAbmDto(it.usuario.id, it.usuario.nombre, it.usuario.apellido, it.usuario.username)
        }
    }

}