package com.estonianport.agendaza.service

import com.estonianport.agendaza.dto.AgendaDto
import com.estonianport.agendaza.dto.EventoAgendaDto
import com.estonianport.agendaza.dto.ConfiguracionDto
import com.estonianport.agendaza.model.Cargo
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Evento
import com.estonianport.agendaza.model.TipoExtra
import com.estonianport.agendaza.model.Usuario
import org.springframework.stereotype.Service

@Service
class AgendaService {

    //TODO refactor a cargo.toAgendaDto
    fun getListaAgendasByUsuario(listaCargo : List<Cargo>): List<AgendaDto> {
        return listaCargo.map { AgendaDto(it.empresa.id, it.empresa.nombre, it.tipoCargo.toString()) }
    }

    //TODO refactor a cargo.EventoAgendaDto
    fun getAllEventosForAgendaByEmpresaId(listaEvento: List<Evento>): List<EventoAgendaDto> {
        return listaEvento.filter{ it.fechaBaja == null }.map { EventoAgendaDto(it.id, it.nombre, it.inicio, it.fin)  }
    }

    fun getAllCantidadesConfiguracionByUsuarioAndEmpresa(usuario: Usuario, empresa: Empresa): ConfiguracionDto {
        return ConfiguracionDto(
            empresa.listaEmpleados.size,
            usuario.listaCargo.size, // TODO sacar de este panel y llevar a admin de usuario
            empresa.listaTipoEvento.filter { it.fechaBaja == null }.size,
            empresa.listaExtra.filter  { (it.tipoExtra == TipoExtra.EVENTO || it.tipoExtra == TipoExtra.VARIABLE_EVENTO) && it.fechaBaja == null }.size,
            empresa.listaEvento.sumOf{ it.listaPago.filter { it.fechaBaja == null }.size },
            empresa.listaEvento.filter { it.fechaBaja == null }.size,
            empresa.listaEvento.filter { it.fechaBaja == null }.map { it.cliente }.toSet().size,
            empresa.listaExtra.filter  { (it.tipoExtra == TipoExtra.TIPO_CATERING || it.tipoExtra == TipoExtra.VARIABLE_CATERING) && it.fechaBaja == null }.size,
            empresa.listaServicio.filter { it.fechaBaja == null }.size)
    }
}
