package com.estonianport.agendaza.service;

import com.estonianport.agendaza.commons.genericService.GenericService;
import com.estonianport.agendaza.model.TipoEvento;

public interface TipoEventoService extends GenericService<TipoEvento, Long>{

	Long count();

}
