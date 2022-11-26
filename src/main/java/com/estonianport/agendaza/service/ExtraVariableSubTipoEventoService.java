package com.estonianport.agendaza.service;

import com.estonianport.agendaza.commons.genericService.GenericService;
import com.estonianport.agendaza.model.ExtraVariableSubTipoEvento;

public interface ExtraVariableSubTipoEventoService extends GenericService<ExtraVariableSubTipoEvento, Long>{

	Long count();

	ExtraVariableSubTipoEvento getExtraVariableSubTipoEventoByNombre(String nombre);

}
