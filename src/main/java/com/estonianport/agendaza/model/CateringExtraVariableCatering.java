package com.estonianport.agendaza.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
public class CateringExtraVariableCatering {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "catering_id")
	private Catering catering;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "extra_variable_catering_id")
	private ExtraVariableCatering extraVariableCatering;

	@Column
	private int cantidad;

}
