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


    //TODO refactor a cargo.EventoAgendaDto
    fun getAllEventosForAgendaByEmpresaId(listaEvento: List<Evento>): List<EventoAgendaDto> {
        return listaEvento.filter{ it.fechaBaja == null }.map { EventoAgendaDto(it.id, it.nombre, it.inicio, it.fin)  }
    }
}
