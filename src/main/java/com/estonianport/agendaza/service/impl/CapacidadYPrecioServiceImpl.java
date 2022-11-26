package com.estonianport.agendaza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.estonianport.agendaza.commons.genericService.GenericServiceImpl;
import com.estonianport.agendaza.dao.CapacidadYPrecioDao;
import com.estonianport.agendaza.model.Catering;
import com.estonianport.agendaza.service.CapacidadYPrecioService;

@Service
public class CapacidadYPrecioServiceImpl  extends GenericServiceImpl<Catering, Long> implements CapacidadYPrecioService{

	@Autowired
	private CapacidadYPrecioDao capacidadYPrecioDao;

	@Override
	public CrudRepository<Catering, Long> getDao() {
		return capacidadYPrecioDao;
	}

}
