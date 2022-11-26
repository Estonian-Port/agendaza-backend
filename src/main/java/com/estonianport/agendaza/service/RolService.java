package com.estonianport.agendaza.service;

import com.estonianport.agendaza.commons.genericService.GenericService;
import com.estonianport.agendaza.model.Rol;

public interface RolService extends GenericService<Rol, Long>{
	
	Rol getRolByNombre(String nombre);

}
