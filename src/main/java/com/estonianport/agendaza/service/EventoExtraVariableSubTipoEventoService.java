package com.estonianport.agendaza.service;

import java.util.List;

import com.estonianport.agendaza.commons.genericService.GenericService;
import com.estonianport.agendaza.model.Evento;
import com.estonianport.agendaza.model.EventoExtraVariableSubTipoEvento;

public interface EventoExtraVariableSubTipoEventoService  extends GenericService<EventoExtraVariableSubTipoEvento, Long>{

	List<EventoExtraVariableSubTipoEvento> getEventoExtraVariableSubTipoEventoByEvento(Evento evento);

}
