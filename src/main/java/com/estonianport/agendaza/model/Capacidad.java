package com.estonianport.agendaza.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Capacidad {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "capacidad_adultos")
	private int capacidadAdultos;

	@Column(name = "capacidad_ninos")
	private int capacidadNinos;

}
