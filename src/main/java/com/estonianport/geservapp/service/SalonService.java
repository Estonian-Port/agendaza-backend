package com.estonianport.geservapp.service;

import com.estonianport.geservapp.commons.genericService.GenericService;
import com.estonianport.geservapp.model.Salon;

public interface SalonService extends GenericService<Salon, Long>{

	Long count();

}
