package com.estonianport.agendaza.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.Getter;
import lombok.Setter;

@Entity(name="precio_con_fecha_extra_variable_catering")
@Getter
@Setter
public class PrecioConFechaExtraVariableCatering extends PrecioConFecha {

	@JsonBackReference
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "extra_variable_catering_id")
	private ExtraVariableCatering extraVariableCatering;
	
}
