package com.estonianport.agendaza.service

import com.estonianport.agendaza.dto.AgendaDto
import com.estonianport.agendaza.dto.AgendaEventoDto
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Usuario
import org.springframework.stereotype.Service

@Service
class AgendaService {

    fun getListaAgendasByUsuario(usuario : Usuario): MutableSet<AgendaDto> {
        var listaAgendaDto : MutableSet<AgendaDto> = mutableSetOf()

        usuario.listaCargo.forEach {
            var agendaDto = AgendaDto(it.empresa.id, it.empresa.nombre, it.tipoCargo.tipoCargoNombre.toString())

            listaAgendaDto.add(agendaDto)
        }

        return listaAgendaDto
    }

    fun getAllEventosByEmpresa(empresa : Empresa): MutableSet<AgendaEventoDto> {

        var listaAgendaEventoDto : MutableSet<AgendaEventoDto> = mutableSetOf()

        empresa.listaEvento.forEach {
            var agendaEventoDto = AgendaEventoDto(it.id, it.nombre, it.inicio, it.fin)

            listaAgendaEventoDto.add(agendaEventoDto)
        }

        return listaAgendaEventoDto
    }
}
