package com.estonianport.agendaza.service

import com.estonianport.agendaza.dto.AgendaDto
import com.estonianport.agendaza.dto.EventoAgendaDto
import com.estonianport.agendaza.dto.CantidadesPanelAdmin
import com.estonianport.agendaza.model.Cargo
import com.estonianport.agendaza.model.Evento
import com.estonianport.agendaza.repository.EmpresaRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AgendaService {
    fun formatEventoToEventoAgendaDto(listaEvento: List<Evento>): List<EventoAgendaDto> {
        return listaEvento.map { EventoAgendaDto(it.id, it.nombre, it.inicio, it.fin)  }.toList()
    }
}
