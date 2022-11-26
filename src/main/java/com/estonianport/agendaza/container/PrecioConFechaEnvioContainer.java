package com.estonianport.agendaza.container;

import java.util.List;

import com.estonianport.agendaza.model.Salon;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrecioConFechaEnvioContainer {

	private Long id;

	private List<PrecioConFechaContainer> precioConFecha;
	
	private Salon salon;
}
