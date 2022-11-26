package com.estonianport.agendaza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.estonianport.agendaza.commons.genericService.GenericServiceImpl;
import com.estonianport.agendaza.dao.PrecioConFechaSubTipoEventoDao;
import com.estonianport.agendaza.model.PrecioConFechaSubTipoEvento;
import com.estonianport.agendaza.service.PrecioConFechaSubTipoEventoService;

@Service
public class PrecioConFechaSubTipoEventoServiceImpl  extends GenericServiceImpl<PrecioConFechaSubTipoEvento, Long> implements PrecioConFechaSubTipoEventoService{

	@Autowired
	private PrecioConFechaSubTipoEventoDao precioConFechaSubTipoEventoFechaDao;

	@Override
	public CrudRepository<PrecioConFechaSubTipoEvento, Long> getDao() {
		return precioConFechaSubTipoEventoFechaDao;
	}

}
