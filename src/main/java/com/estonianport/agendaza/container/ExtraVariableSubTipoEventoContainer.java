package com.estonianport.agendaza.container;

import java.util.List;

import com.estonianport.agendaza.model.ExtraVariableSubTipoEvento;
import com.estonianport.agendaza.model.Salon;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExtraVariableSubTipoEventoContainer {

	private ExtraVariableSubTipoEvento extraVariableSubTipoEvento;

	private List<PrecioConFechaContainer> precioConFecha;
	
	private Salon salon;
}
