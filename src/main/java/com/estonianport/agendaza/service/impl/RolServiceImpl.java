package com.estonianport.agendaza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.estonianport.agendaza.commons.genericService.GenericServiceImpl;
import com.estonianport.agendaza.dao.RolDao;
import com.estonianport.agendaza.model.Rol;
import com.estonianport.agendaza.service.RolService;

@Service
public class RolServiceImpl extends GenericServiceImpl<Rol, Long> implements RolService{

	@Autowired
	private RolDao rolDao;

	@Override
	public CrudRepository<Rol, Long> getDao() {
		return rolDao;
	}

	@Override
	public Rol getRolByNombre(String nombre) {
		return rolDao.getRolByNombre(nombre);
	}
}