package com.estonianport.agendaza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.estonianport.agendaza.commons.genericService.GenericServiceImpl;
import com.estonianport.agendaza.dao.CateringDao;
import com.estonianport.agendaza.model.Catering;
import com.estonianport.agendaza.service.CateringService;

@Service
public class CateringServiceImpl  extends GenericServiceImpl<Catering, Long> implements CateringService{

	@Autowired
	private CateringDao cateringDao;

	@Override
	public CrudRepository<Catering, Long> getDao() {
		return cateringDao;
	}

}
