package com.estonianport.agendaza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.estonianport.agendaza.commons.genericService.GenericServiceImpl;
import com.estonianport.agendaza.dao.MedioDePagoDao;
import com.estonianport.agendaza.model.MedioDePago;
import com.estonianport.agendaza.service.MedioDePagoService;

@Service
public class MedioDePagoServiceImpl extends GenericServiceImpl<MedioDePago, Long> implements MedioDePagoService{

	@Autowired
	private MedioDePagoDao medioDePagoDao;

	@Override
	public CrudRepository<MedioDePago, Long> getDao() {
		return medioDePagoDao;
	}
	
	public Long count() {
		return medioDePagoDao.count();
	}

}
