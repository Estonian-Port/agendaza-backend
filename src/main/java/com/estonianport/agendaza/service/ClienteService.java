package com.estonianport.agendaza.service;

import com.estonianport.agendaza.commons.genericService.GenericService;
import com.estonianport.agendaza.model.Cliente;

public interface ClienteService  extends GenericService<Cliente, Long>{

	Long count();

	boolean existsByCuil(long cuil);

	Cliente getClienteByCuil(long cuil);

}
