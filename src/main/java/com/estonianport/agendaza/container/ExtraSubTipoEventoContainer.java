package com.estonianport.agendaza.container;

import java.util.List;

import com.estonianport.agendaza.model.ExtraSubTipoEvento;
import com.estonianport.agendaza.model.Salon;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExtraSubTipoEventoContainer {

	private ExtraSubTipoEvento extraSubTipoEvento;

	private List<PrecioConFechaContainer> precioConFecha;
	
	private Salon salon;
}
