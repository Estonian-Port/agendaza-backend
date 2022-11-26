package com.estonianport.agendaza.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.estonianport.agendaza.commons.genericService.GenericServiceImpl;
import com.estonianport.agendaza.dao.PagoDao;
import com.estonianport.agendaza.model.Evento;
import com.estonianport.agendaza.model.Pago;
import com.estonianport.agendaza.service.PagoService;

@Service
public class PagoServiceImpl extends GenericServiceImpl<Pago, Long> implements PagoService{

	@Autowired
	private PagoDao pagoDao;

	@Override
	public CrudRepository<Pago, Long> getDao() {
		return pagoDao;
	}

	@Override
	public List<Pago> findPagosByEvento(Evento evento) {
		return pagoDao.findPagosByEvento(evento);
	}

	public Long count() {
		return pagoDao.count();
	}
}
