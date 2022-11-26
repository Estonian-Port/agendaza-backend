package com.estonianport.agendaza.container;

import java.util.List;
import java.util.Set;

import com.estonianport.agendaza.model.CateringExtraVariableCatering;
import com.estonianport.agendaza.model.Cliente;
import com.estonianport.agendaza.model.Evento;
import com.estonianport.agendaza.model.EventoExtraVariableSubTipoEvento;
import com.estonianport.agendaza.model.ExtraSubTipoEvento;
import com.estonianport.agendaza.model.ExtraVariableCatering;
import com.estonianport.agendaza.model.ExtraVariableSubTipoEvento;
import com.estonianport.agendaza.model.TipoCatering;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservaContainer {

	private Evento evento;

	private Cliente cliente;

	private Set<ExtraSubTipoEvento> extraSubTipoEvento;
	
	private Set<ExtraVariableSubTipoEvento> extraVariableSubTipoEvento;

	private Set<ExtraVariableCatering> extraCatering;
	
	private Set<TipoCatering> tipoCatering;
	
	private List<EventoExtraVariableSubTipoEvento> eventoExtraVariableSubTipoEvento;
	
	private List<CateringExtraVariableCatering> cateringExtraVariableCatering;

	private String fecha;

	private String inicio;

	private String fin;

	private Boolean hastaElOtroDia;
	
	private Boolean resto24;

}
