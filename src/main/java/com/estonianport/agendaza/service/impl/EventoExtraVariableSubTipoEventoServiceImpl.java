package com.estonianport.agendaza.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.estonianport.agendaza.commons.genericService.GenericServiceImpl;
import com.estonianport.agendaza.dao.EventoExtraVariableSubTipoEventoDao;
import com.estonianport.agendaza.model.Evento;
import com.estonianport.agendaza.model.EventoExtraVariableSubTipoEvento;
import com.estonianport.agendaza.service.EventoExtraVariableSubTipoEventoService;

@Service
public class EventoExtraVariableSubTipoEventoServiceImpl extends GenericServiceImpl<EventoExtraVariableSubTipoEvento, Long> implements EventoExtraVariableSubTipoEventoService{

	@Autowired
	private EventoExtraVariableSubTipoEventoDao eventoExtraVariableSubTipoEventoDao;

	@Override
	public CrudRepository<EventoExtraVariableSubTipoEvento, Long> getDao() {
		return eventoExtraVariableSubTipoEventoDao;
	}

	@Override
	public List<EventoExtraVariableSubTipoEvento> getEventoExtraVariableSubTipoEventoByEvento(Evento evento) {
		return eventoExtraVariableSubTipoEventoDao.getEventoExtraVariableSubTipoEventoByEvento(evento);
	}

}
