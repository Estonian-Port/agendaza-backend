package com.estonianport.agendaza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.estonianport.agendaza.commons.genericService.GenericServiceImpl;
import com.estonianport.agendaza.dao.PrecioConFechaExtraVariableCateringDao;
import com.estonianport.agendaza.model.PrecioConFechaExtraVariableCatering;
import com.estonianport.agendaza.service.PrecioConFechaExtraVariableCateringService;

@Service
public class PrecioConFechaExtraVariableCateringServiceImpl  extends GenericServiceImpl<PrecioConFechaExtraVariableCatering, Long> implements PrecioConFechaExtraVariableCateringService{

	@Autowired
	private PrecioConFechaExtraVariableCateringDao precioConFechaExtraVariableCateringDao;

	@Override
	public CrudRepository<PrecioConFechaExtraVariableCatering, Long> getDao() {
		return precioConFechaExtraVariableCateringDao;
	}

}
