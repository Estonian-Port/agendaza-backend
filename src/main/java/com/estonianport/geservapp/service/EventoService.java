package com.estonianport.geservapp.service;

import java.util.List;

import com.estonianport.geservapp.commons.GenericService;
import com.estonianport.geservapp.model.Evento;
import com.estonianport.geservapp.model.Salon;

public interface EventoService extends GenericService<Evento, Long>{

	List<Evento> getEventosBySalon(Salon salon);

}