package com.estonianport.agendaza.service;

import java.util.List;

import com.estonianport.agendaza.commons.genericService.GenericService;
import com.estonianport.agendaza.model.Evento;
import com.estonianport.agendaza.model.Pago;

public interface PagoService extends GenericService<Pago, Long>{

	List<Pago> findPagosByEvento(Evento evento);

	Long count();

}
