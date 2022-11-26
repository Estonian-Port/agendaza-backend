package com.estonianport.agendaza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.estonianport.agendaza.commons.genericService.GenericServiceImpl;
import com.estonianport.agendaza.dao.ClienteDao;
import com.estonianport.agendaza.model.Cliente;
import com.estonianport.agendaza.service.ClienteService;

@Service
public class ClienteServiceImpl  extends GenericServiceImpl<Cliente, Long> implements ClienteService{

	@Autowired
	private ClienteDao clienteDao;

	@Override
	public CrudRepository<Cliente, Long> getDao() {
		return clienteDao;
	}

	public Long count() {
		return clienteDao.count();
	}

	@Override
	public boolean existsByCuil(long cuil) {
		return clienteDao.existsByCuil(cuil);
	}

	@Override
	public Cliente getClienteByCuil(long cuil) {
		return clienteDao.getClienteByCuil(cuil);
	}
}
