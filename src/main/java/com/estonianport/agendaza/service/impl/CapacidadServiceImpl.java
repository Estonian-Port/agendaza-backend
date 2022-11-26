package com.estonianport.agendaza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.estonianport.agendaza.commons.genericService.GenericServiceImpl;
import com.estonianport.agendaza.dao.CapacidadDao;
import com.estonianport.agendaza.model.Capacidad;
import com.estonianport.agendaza.service.CapacidadService;

@Service
public class CapacidadServiceImpl  extends GenericServiceImpl<Capacidad, Long> implements CapacidadService{

	@Autowired
	private CapacidadDao capacidadDao;

	@Override
	public CrudRepository<Capacidad, Long> getDao() {
		return capacidadDao;
	}

}
