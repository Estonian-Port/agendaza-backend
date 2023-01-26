package com.estonianport.agendaza.service

import com.estonianport.agendaza.dto.AgendaDto
import com.estonianport.agendaza.model.Usuario
import org.springframework.stereotype.Service

@Service
class AgendaService {

    fun getListaAgendasByUsuario(usuario : Usuario): MutableSet<AgendaDto> {
        var listaAgendaDto : MutableSet<AgendaDto> = mutableSetOf()

        usuario.listaCargo.forEach {
            var agendaDto = AgendaDto(it.id, it.empresa.nombre, it.tipoCargo.tipoCargoNombre.toString())

            listaAgendaDto.add(agendaDto)
        }

        return listaAgendaDto
    }
}
