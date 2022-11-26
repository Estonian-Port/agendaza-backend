package com.estonianport.agendaza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.estonianport.agendaza.commons.genericService.GenericServiceImpl;
import com.estonianport.agendaza.dao.SexoDao;
import com.estonianport.agendaza.model.Sexo;
import com.estonianport.agendaza.service.SexoService;

@Service
public class SexoServiceImpl extends GenericServiceImpl<Sexo, Long> implements SexoService{

	@Autowired
	private SexoDao sexoDao;

	@Override
	public CrudRepository<Sexo, Long> getDao() {
		return sexoDao;
	}
}
