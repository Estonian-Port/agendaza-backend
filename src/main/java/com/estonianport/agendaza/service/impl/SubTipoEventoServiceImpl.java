package com.estonianport.agendaza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.estonianport.agendaza.commons.genericService.GenericServiceImpl;
import com.estonianport.agendaza.dao.SubTipoEventoDao;
import com.estonianport.agendaza.model.SubTipoEvento;
import com.estonianport.agendaza.service.SubTipoEventoService;

@Service
public class SubTipoEventoServiceImpl extends GenericServiceImpl<SubTipoEvento, Long> implements SubTipoEventoService{

	@Autowired
	private SubTipoEventoDao subTipoEvento;

	@Override
	public CrudRepository<SubTipoEvento, Long> getDao() {
		return subTipoEvento;
	}
	
	public Long count() {
		return subTipoEvento.count();
	}


}
