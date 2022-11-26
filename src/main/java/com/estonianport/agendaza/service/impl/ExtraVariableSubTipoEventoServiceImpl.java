package com.estonianport.agendaza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.estonianport.agendaza.commons.genericService.GenericServiceImpl;
import com.estonianport.agendaza.dao.ExtraVariableSubTipoEventoDao;
import com.estonianport.agendaza.model.ExtraVariableSubTipoEvento;
import com.estonianport.agendaza.service.ExtraVariableSubTipoEventoService;

@Service
public class ExtraVariableSubTipoEventoServiceImpl extends GenericServiceImpl<ExtraVariableSubTipoEvento, Long> implements ExtraVariableSubTipoEventoService{

	@Autowired
	private ExtraVariableSubTipoEventoDao extraVariableSubTipoEventoDao;

	@Override
	public CrudRepository<ExtraVariableSubTipoEvento, Long> getDao() {
		return extraVariableSubTipoEventoDao;
	}

	public Long count() {
		return extraVariableSubTipoEventoDao.count();
	}

	@Override
	public ExtraVariableSubTipoEvento getExtraVariableSubTipoEventoByNombre(String nombre) {
		return extraVariableSubTipoEventoDao.getExtraVariableSubTipoEventoByNombre(nombre);
	}

}
