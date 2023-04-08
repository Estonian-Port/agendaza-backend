package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.dao.EmpresaDao
import com.estonianport.agendaza.dto.EventoDto
import com.estonianport.agendaza.dto.PagoDto
import com.estonianport.agendaza.dto.UsuarioAbmDto
import com.estonianport.agendaza.model.Pago
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
class EmpresaService : GenericServiceImpl<Empresa, Long>() {

    @Autowired
    lateinit var EmpresaDao: EmpresaDao

    override val dao: CrudRepository<Empresa, Long>
        get() = EmpresaDao

    fun getAllEventoByEmpresaId(empresa : Empresa): MutableSet<EventoDto> {

        val listaAgendaEventoDto : MutableSet<EventoDto> = mutableSetOf()

        empresa.listaEvento.forEach {
            listaAgendaEventoDto.add(EventoDto(it.id, it.nombre, it.codigo, it.inicio, it.fin, it.tipoEvento.nombre))
        }

        return listaAgendaEventoDto
    }

    fun getAllPagoByEmpresaId(empresa : Empresa): MutableSet<PagoDto> {

        val listaPago : MutableSet<PagoDto> = mutableSetOf()

        empresa.listaEvento.forEach {
            it.listaPago.forEach {
                listaPago.add(PagoDto(it.id, it.monto,it.evento.codigo, it.medioDePago,
                    it.evento.nombre, it.fecha))
            }
        }

        return listaPago
    }

    fun getAllUsuariosByEmpresaId(empresa: Empresa): MutableSet<UsuarioAbmDto> {
        val listaUsuarioAbmDto : MutableSet<UsuarioAbmDto> = mutableSetOf()

        empresa.listaEmpleados.forEach {
            listaUsuarioAbmDto.add(UsuarioAbmDto(it.usuario.id, it.usuario.nombre, it.usuario.apellido, it.usuario.username))
        }

        return listaUsuarioAbmDto
    }

}