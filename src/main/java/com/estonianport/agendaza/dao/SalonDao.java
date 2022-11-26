package com.estonianport.agendaza.dao;

import org.springframework.data.repository.CrudRepository;

import com.estonianport.agendaza.model.Salon;

public interface SalonDao extends CrudRepository<Salon, Long> {

}
