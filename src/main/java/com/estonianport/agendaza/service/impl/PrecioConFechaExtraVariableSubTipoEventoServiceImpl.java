package com.estonianport.agendaza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.estonianport.agendaza.commons.genericService.GenericServiceImpl;
import com.estonianport.agendaza.dao.PrecioConFechaExtraVariableSubTipoEventoDao;
import com.estonianport.agendaza.model.PrecioConFechaExtraVariableSubTipoEvento;
import com.estonianport.agendaza.service.PrecioConFechaExtraVariableSubTipoEventoService;

@Service
public class PrecioConFechaExtraVariableSubTipoEventoServiceImpl  extends GenericServiceImpl<PrecioConFechaExtraVariableSubTipoEvento, Long> implements PrecioConFechaExtraVariableSubTipoEventoService{

	@Autowired
	private PrecioConFechaExtraVariableSubTipoEventoDao precioConFechaExtraVariableSubTipoEventoDao;

	@Override
	public CrudRepository<PrecioConFechaExtraVariableSubTipoEvento, Long> getDao() {
		return precioConFechaExtraVariableSubTipoEventoDao;
	}

}
