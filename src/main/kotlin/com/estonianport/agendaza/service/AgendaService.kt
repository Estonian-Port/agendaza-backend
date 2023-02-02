package com.estonianport.agendaza.service

import com.estonianport.agendaza.dto.AgendaDto
import com.estonianport.agendaza.dto.AgendaEventoDto
import com.estonianport.agendaza.dto.ConfiguracionDto
import com.estonianport.agendaza.dto.UsuarioAbmDto
import com.estonianport.agendaza.model.Empresa
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

    fun getAllEventosByEmpresa(empresa : Empresa): MutableSet<AgendaEventoDto> {

        val listaAgendaEventoDto : MutableSet<AgendaEventoDto> = mutableSetOf()

        empresa.listaEvento.forEach {
            listaAgendaEventoDto.add(AgendaEventoDto(it.id, it.nombre, it.inicio, it.fin))
        }

        return listaAgendaEventoDto
    }

    fun getAllCantidadesConfiguracionByEmpresa(empresa: Empresa): ConfiguracionDto {
        return ConfiguracionDto(empresa.listaEmpleados.size, 0, empresa.listaTipoEvento.size,
            empresa.listaExtra.size, 0, empresa.listaEvento.size, 0, 0, 0)
    }

    fun getAllEmpleadosByEmpresaId(empresa: Empresa): MutableSet<UsuarioAbmDto> {
        val listaUsuarioAbmDto : MutableSet<UsuarioAbmDto> = mutableSetOf()

         empresa.listaEmpleados.forEach {
             listaUsuarioAbmDto.add(UsuarioAbmDto(it.usuario.id, it.usuario.nombre, it.usuario.apellido, it.usuario.username))
         }

        return listaUsuarioAbmDto
    }
}
