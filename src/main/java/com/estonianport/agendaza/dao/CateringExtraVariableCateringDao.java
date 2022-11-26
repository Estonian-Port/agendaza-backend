package com.estonianport.agendaza.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.estonianport.agendaza.model.Catering;
import com.estonianport.agendaza.model.CateringExtraVariableCatering;

public interface CateringExtraVariableCateringDao extends CrudRepository<CateringExtraVariableCatering, Long> {

	List<CateringExtraVariableCatering> getCateringExtraVariableCateringByCatering(Catering catering);

}
