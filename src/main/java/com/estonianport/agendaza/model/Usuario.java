package com.estonianport.agendaza.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Usuario {


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String nombre;

	@Column
	private String apellido;

	@Column
	private String username;

	@Column
	private String password;

	@Column
	private String mail;

	@Column
	private boolean accountNonExpired;

	@Column
	private boolean accountNonLocked;

	@Column
	private boolean credentialsNonExpired;

	@Column
	private boolean enabled;

	@ManyToOne
	@JoinColumn(name = "rol_id")
	private Rol rol;


}
