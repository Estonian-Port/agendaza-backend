package com.estonianport.agendaza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.estonianport.agendaza.commons.genericService.GenericServiceImpl;
import com.estonianport.agendaza.dao.PrecioConFechaTipoCateringDao;
import com.estonianport.agendaza.model.PrecioConFechaTipoCatering;
import com.estonianport.agendaza.service.PrecioConFechaTipoCateringService;

@Service
public class PrecioConFechaTipoCateringServiceImpl  extends GenericServiceImpl<PrecioConFechaTipoCatering, Long> implements PrecioConFechaTipoCateringService{

	@Autowired
	private PrecioConFechaTipoCateringDao precioConFechaTipoCateringDao;

	@Override
	public CrudRepository<PrecioConFechaTipoCatering, Long> getDao() {
		return precioConFechaTipoCateringDao;
	}

}
