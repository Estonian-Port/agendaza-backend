package com.estonianport.agendaza.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.estonianport.agendaza.commons.genericService.GenericServiceImpl;
import com.estonianport.agendaza.dao.CateringExtraVariableCateringDao;
import com.estonianport.agendaza.model.Catering;
import com.estonianport.agendaza.model.CateringExtraVariableCatering;
import com.estonianport.agendaza.service.CateringExtraVariableCateringService;

@Service
public class CateringExtraVariableCateringServiceImpl  extends GenericServiceImpl<CateringExtraVariableCatering, Long> implements CateringExtraVariableCateringService{

	@Autowired
	private CateringExtraVariableCateringDao cateringExtraVariableCateringDao;

	@Override
	public CrudRepository<CateringExtraVariableCatering, Long> getDao() {
		return cateringExtraVariableCateringDao;
	}

	@Override
	public List<CateringExtraVariableCatering> getCateringExtraVariableCateringByCatering(Catering catering) {
		return cateringExtraVariableCateringDao.getCateringExtraVariableCateringByCatering(catering);
	}

}
