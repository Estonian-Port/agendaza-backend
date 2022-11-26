package com.estonianport.agendaza.service;

import com.estonianport.agendaza.commons.genericService.GenericService;
import com.estonianport.agendaza.model.Usuario;

public interface UsuarioService extends GenericService<Usuario, Long>{

	public Usuario findUserByUsername(String username);
	
	public Long count();
}
