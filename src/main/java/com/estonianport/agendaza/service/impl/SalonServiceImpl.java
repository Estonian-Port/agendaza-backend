package com.estonianport.agendaza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.estonianport.agendaza.commons.genericService.GenericServiceImpl;
import com.estonianport.agendaza.dao.SalonDao;
import com.estonianport.agendaza.model.Salon;
import com.estonianport.agendaza.service.SalonService;

@Service
public class SalonServiceImpl extends GenericServiceImpl<Salon, Long> implements SalonService{

	@Autowired
	private SalonDao salonDao;

	@Override
	public CrudRepository<Salon, Long> getDao() {
		return salonDao;
	}
	
	public Long count() {
		return salonDao.count();
	}

}
