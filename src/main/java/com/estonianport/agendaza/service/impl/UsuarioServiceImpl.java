package com.estonianport.agendaza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.estonianport.agendaza.commons.genericService.GenericServiceImpl;
import com.estonianport.agendaza.dao.UsuarioDao;
import com.estonianport.agendaza.model.Usuario;
import com.estonianport.agendaza.service.UsuarioService;

@Service
public class UsuarioServiceImpl extends GenericServiceImpl<Usuario, Long> implements UserDetailsService, UsuarioService{

	@Autowired
	UsuarioDao usuarioDao;

	@Override
	public CrudRepository<Usuario, Long> getDao() {
		return usuarioDao;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Usuario usuario = usuarioDao.findUserByUsername(username);
		if (usuario == null) {
			throw new UsernameNotFoundException("El usuario no existe");
		}
		return usuario;
	}
	
	public Usuario findUserByUsername(String username) {
		return usuarioDao.findUserByUsername(username);
	}
	
	public Long count() {
		return usuarioDao.count();
	}
}