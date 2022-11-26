package com.estonianport.agendaza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.estonianport.agendaza.commons.genericService.GenericServiceImpl;
import com.estonianport.agendaza.dao.ExtraSubTipoEventoDao;
import com.estonianport.agendaza.model.ExtraSubTipoEvento;
import com.estonianport.agendaza.service.ExtraSubTipoEventoService;

@Service
public class ExtraSubTipoEventoServiceImpl extends GenericServiceImpl<ExtraSubTipoEvento, Long> implements ExtraSubTipoEventoService{

	@Autowired
	private ExtraSubTipoEventoDao extraSubTipoEventoDao;

	@Override
	public CrudRepository<ExtraSubTipoEvento, Long> getDao() {
		return extraSubTipoEventoDao;
	}

	public Long count() {
		return extraSubTipoEventoDao.count();
	}

}
