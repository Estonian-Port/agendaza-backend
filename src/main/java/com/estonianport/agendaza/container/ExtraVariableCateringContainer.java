package com.estonianport.agendaza.container;

import java.util.List;

import com.estonianport.agendaza.model.ExtraVariableCatering;
import com.estonianport.agendaza.model.Salon;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExtraVariableCateringContainer {

	private ExtraVariableCatering extraVariableCatering;

	private List<PrecioConFechaContainer> precioConFecha;
	
	private Salon salon;
}
