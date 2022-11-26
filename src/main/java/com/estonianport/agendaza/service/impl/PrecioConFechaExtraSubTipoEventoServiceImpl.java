package com.estonianport.agendaza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.estonianport.agendaza.commons.genericService.GenericServiceImpl;
import com.estonianport.agendaza.dao.PrecioConFechaExtraSubTipoEventoDao;
import com.estonianport.agendaza.model.PrecioConFechaExtraSubTipoEvento;
import com.estonianport.agendaza.service.PrecioConFechaExtraSubTipoEventoService;

@Service
public class PrecioConFechaExtraSubTipoEventoServiceImpl  extends GenericServiceImpl<PrecioConFechaExtraSubTipoEvento, Long> implements PrecioConFechaExtraSubTipoEventoService{

	@Autowired
	private PrecioConFechaExtraSubTipoEventoDao precioConFechaExtraSubTipoEventoDao;

	@Override
	public CrudRepository<PrecioConFechaExtraSubTipoEvento, Long> getDao() {
		return precioConFechaExtraSubTipoEventoDao;
	}

}
