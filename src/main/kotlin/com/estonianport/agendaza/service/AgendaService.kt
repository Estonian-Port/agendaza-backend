package com.estonianport.agendaza.service

import com.estonianport.agendaza.dto.AgendaDto
import com.estonianport.agendaza.dto.EventoAgendaDto
import com.estonianport.agendaza.dto.ConfiguracionDto
import com.estonianport.agendaza.dto.UsuarioAbmDto
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.TipoExtra
import com.estonianport.agendaza.model.Usuario
import org.springframework.stereotype.Service

@Service
class AgendaService {

    //TODO refactor a cargo.toAgendaDto
    fun getListaAgendasByUsuario(usuario: Usuario): List<AgendaDto> {
        return usuario.listaCargo.map { AgendaDto(it.empresa.id, it.empresa.nombre, it.tipoCargo.toString()) }
    }

    //TODO refactor a cargo.EventoAgendaDto
    fun getAllEventosForAgendaByEmpresaId(empresa : Empresa): List<EventoAgendaDto> {
        return empresa.listaEvento.map { EventoAgendaDto(it.id, it.nombre, it.inicio, it.fin)  }
    }

    fun getAllCantidadesConfiguracionByUsuarioAndEmpresa(usuario: Usuario, empresa: Empresa): ConfiguracionDto {
        return ConfiguracionDto(
            empresa.listaEmpleados.size,
            usuario.listaCargo.size,
            empresa.listaTipoEvento.size,
            empresa.listaExtra.filter  { it.tipoExtra == TipoExtra.EVENTO || it.tipoExtra == TipoExtra.VARIABLE_EVENTO }.size,
            empresa.listaEvento.sumOf{ it.listaPago.size },
            empresa.listaEvento.size,
            empresa.listaEvento.map { it.cliente }.toSet().size,
            empresa.listaExtra.filter  { it.tipoExtra == TipoExtra.TIPO_CATERING || it.tipoExtra == TipoExtra.VARIABLE_CATERING }.size,
            empresa.listaServicio.size)
    }
}
