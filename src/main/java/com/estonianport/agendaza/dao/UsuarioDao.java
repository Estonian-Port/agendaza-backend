package com.estonianport.agendaza.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.estonianport.agendaza.model.Usuario;

public interface UsuarioDao extends JpaRepository<Usuario,Long>{

	Usuario findUserByUsername(String username);

}
