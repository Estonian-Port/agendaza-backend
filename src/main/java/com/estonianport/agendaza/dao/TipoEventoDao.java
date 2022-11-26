package com.estonianport.agendaza.dao;

import org.springframework.data.repository.CrudRepository;

import com.estonianport.agendaza.model.TipoEvento;

public interface TipoEventoDao extends CrudRepository<TipoEvento, Long> {

}
