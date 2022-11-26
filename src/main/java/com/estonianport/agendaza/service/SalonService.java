package com.estonianport.agendaza.service;

import com.estonianport.agendaza.commons.genericService.GenericService;
import com.estonianport.agendaza.model.Salon;

public interface SalonService extends GenericService<Salon, Long>{

	Long count();

}
