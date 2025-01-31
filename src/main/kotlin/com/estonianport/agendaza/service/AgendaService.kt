package com.estonianport.agendaza.service

import com.estonianport.agendaza.dto.EventoAgendaDTO
import com.estonianport.agendaza.model.Evento
import org.springframework.stereotype.Service

@Service
class AgendaService {
    fun formatEventoToEventoAgendaDto(listaEvento: List<Evento>): List<EventoAgendaDTO> {
        return listaEvento.map { EventoAgendaDTO(it.id, it.nombre, it.inicio, it.fin)  }.toList()
    }
}
