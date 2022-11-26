package com.estonianport.agendaza.model;

import java.sql.Date;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Cliente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String nombre;

	@Column
	private String apellido;

	@ManyToOne
	@JoinColumn(name = "sexo_id")
	private Sexo sexo;

	@Column
	private long cuil;

	@Column(name = "fecha_nacimiento")
	private Date fechaNacimiento;

	@Column
	private String empresa;

	@Column
	private String ciudad;

	@Column
	private String provincia;

	@Column
	private int codigoPostal;

	@Column
	private String email;

	@Column
	private long celular;
	
	// TODO cambiar nombre evento a listaEvento
	@JsonBackReference
	@OneToMany(mappedBy = "cliente")
	private Set<Evento> evento;

}
