package com.estonianport.agendaza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.estonianport.agendaza.commons.genericService.GenericServiceImpl;
import com.estonianport.agendaza.dao.ServicioDao;
import com.estonianport.agendaza.model.Servicio;
import com.estonianport.agendaza.service.ServicioService;

@Service
public class ServicioServiceImpl extends GenericServiceImpl<Servicio, Long> implements ServicioService{

	@Autowired
	private ServicioDao servicioDao;

	@Override
	public CrudRepository<Servicio, Long> getDao() {
		return servicioDao;
	}

	public Long count() {
		return servicioDao.count();
	}

}
