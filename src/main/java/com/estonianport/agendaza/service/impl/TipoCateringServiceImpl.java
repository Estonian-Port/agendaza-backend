package com.estonianport.agendaza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.estonianport.agendaza.commons.genericService.GenericServiceImpl;
import com.estonianport.agendaza.dao.TipoCateringDao;
import com.estonianport.agendaza.model.TipoCatering;
import com.estonianport.agendaza.service.TipoCateringService;

@Service
public class TipoCateringServiceImpl extends GenericServiceImpl<TipoCatering, Long> implements TipoCateringService{

	@Autowired
	private TipoCateringDao tipoCateringDao;

	@Override
	public CrudRepository<TipoCatering, Long> getDao() {
		return tipoCateringDao;
	}

	public Long count() {
		return tipoCateringDao.count();
	}

}
