package com.estonianport.agendaza.dao;

import org.springframework.data.repository.CrudRepository;

import com.estonianport.agendaza.model.Rol;

public interface RolDao extends CrudRepository<Rol, Long> {

	Rol getRolByNombre(String nombre);
	
}
