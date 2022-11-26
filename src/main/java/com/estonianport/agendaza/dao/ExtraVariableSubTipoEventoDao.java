package com.estonianport.agendaza.dao;

import org.springframework.data.repository.CrudRepository;

import com.estonianport.agendaza.model.ExtraVariableSubTipoEvento;

public interface ExtraVariableSubTipoEventoDao extends CrudRepository<ExtraVariableSubTipoEvento, Long> {

	ExtraVariableSubTipoEvento getExtraVariableSubTipoEventoByNombre(String nombre);
	
}
