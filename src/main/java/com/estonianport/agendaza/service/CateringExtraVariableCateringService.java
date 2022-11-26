package com.estonianport.agendaza.service;

import java.util.List;

import com.estonianport.agendaza.commons.genericService.GenericService;
import com.estonianport.agendaza.model.Catering;
import com.estonianport.agendaza.model.CateringExtraVariableCatering;

public interface CateringExtraVariableCateringService  extends GenericService<CateringExtraVariableCatering, Long>{

	List<CateringExtraVariableCatering> getCateringExtraVariableCateringByCatering(Catering catering);

}
