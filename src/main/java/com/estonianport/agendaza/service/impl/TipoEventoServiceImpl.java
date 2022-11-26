package com.estonianport.agendaza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.estonianport.agendaza.commons.genericService.GenericServiceImpl;
import com.estonianport.agendaza.dao.TipoEventoDao;
import com.estonianport.agendaza.model.TipoEvento;
import com.estonianport.agendaza.service.TipoEventoService;

@Service
public class TipoEventoServiceImpl extends GenericServiceImpl<TipoEvento, Long> implements TipoEventoService{

	@Autowired
	private TipoEventoDao tipoEvento;

	@Override
	public CrudRepository<TipoEvento, Long> getDao() {
		return tipoEvento;
	}
	
	public Long count() {
		return tipoEvento.count();
	}

}
