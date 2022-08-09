package com.estonianport.geservapp.service;

import com.estonianport.geservapp.commons.genericService.GenericService;
import com.estonianport.geservapp.model.TipoEvento;

public interface TipoEventoService extends GenericService<TipoEvento, Long>{

	Long count();

}
