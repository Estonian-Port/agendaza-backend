package com.estonianport.agendaza.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.estonianport.agendaza.model.Evento;
import com.estonianport.agendaza.model.Pago;

public interface PagoDao extends CrudRepository<Pago, Long> {

	List<Pago> findPagosByEvento(Evento evento);

}
