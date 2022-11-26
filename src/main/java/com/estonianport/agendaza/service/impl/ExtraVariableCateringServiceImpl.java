package com.estonianport.agendaza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.estonianport.agendaza.commons.genericService.GenericServiceImpl;
import com.estonianport.agendaza.dao.ExtraVariableCateringDao;
import com.estonianport.agendaza.model.ExtraVariableCatering;
import com.estonianport.agendaza.service.ExtraVariableCateringService;

@Service
public class ExtraVariableCateringServiceImpl extends GenericServiceImpl<ExtraVariableCatering, Long> implements ExtraVariableCateringService{

	@Autowired
	private ExtraVariableCateringDao extraCateringDao;

	@Override
	public CrudRepository<ExtraVariableCatering, Long> getDao() {
		return extraCateringDao;
	}

	public Long count() {
		return extraCateringDao.count();
	}

}
