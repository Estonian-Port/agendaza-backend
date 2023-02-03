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

    fun getListaAgendasByUsuario(usuario : Usuario): MutableSet<AgendaDto> {
        val listaAgendaDto : MutableSet<AgendaDto> = mutableSetOf()

        usuario.listaCargo.forEach {
            val agendaDto = AgendaDto(it.empresa.id, it.empresa.nombre, it.tipoCargo.tipoCargoNombre.toString())

            listaAgendaDto.add(agendaDto)
        }

        return listaAgendaDto
    }

    fun getAllEventosForAgendaByEmpresaId(empresa : Empresa): MutableSet<EventoAgendaDto> {

        val listaAgendaEventoDto : MutableSet<EventoAgendaDto> = mutableSetOf()

        empresa.listaEvento.forEach {
            listaAgendaEventoDto.add(EventoAgendaDto(it.id, it.nombre, it.inicio, it.fin))
        }

        return listaAgendaEventoDto
    }

    fun getAllCantidadesConfiguracionByUsuarioAndEmpresa(usuario: Usuario, empresa: Empresa): ConfiguracionDto {
        return ConfiguracionDto(
            empresa.listaEmpleados.size,
            usuario.listaCargo.size,
            empresa.listaTipoEvento.size,
            empresa.listaExtra.filter  { it.tipoExtra == TipoExtra.EVENTO || it.tipoExtra == TipoExtra.VARIABLE_EVENTO }.size,
            empresa.listaEvento.sumOf{ it.listaPago.size },
            empresa.listaEvento.size,
            0,
            empresa.listaExtra.filter  { it.tipoExtra == TipoExtra.TIPO_CATERING || it.tipoExtra == TipoExtra.VARIABLE_CATERING }.size,
            empresa.listaServicio.size)
    }

    fun getAllUsuariosByEmpresaId(empresa: Empresa): MutableSet<UsuarioAbmDto> {
        val listaUsuarioAbmDto : MutableSet<UsuarioAbmDto> = mutableSetOf()

         empresa.listaEmpleados.forEach {
             listaUsuarioAbmDto.add(UsuarioAbmDto(it.usuario.id, it.usuario.nombre, it.usuario.apellido, it.usuario.username))
         }

        return listaUsuarioAbmDto
    }
}
