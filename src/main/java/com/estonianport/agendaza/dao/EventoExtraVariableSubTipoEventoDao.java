package com.estonianport.agendaza.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.estonianport.agendaza.model.Evento;
import com.estonianport.agendaza.model.EventoExtraVariableSubTipoEvento;

public interface EventoExtraVariableSubTipoEventoDao extends CrudRepository<EventoExtraVariableSubTipoEvento, Long> {

	List<EventoExtraVariableSubTipoEvento> getEventoExtraVariableSubTipoEventoByEvento(Evento evento);

}
