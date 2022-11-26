package com.estonianport.agendaza.dao;

import org.springframework.data.repository.CrudRepository;

import com.estonianport.agendaza.model.Cliente;

public interface ClienteDao extends CrudRepository<Cliente, Long> {

	boolean existsByCuil(long cuil);

	Cliente getClienteByCuil(long cuil);

}
