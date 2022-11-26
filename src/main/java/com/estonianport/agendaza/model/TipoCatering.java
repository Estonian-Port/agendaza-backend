package com.estonianport.agendaza.model;

import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Getter;
import lombok.Setter;

@Entity(name="tipo_catering")
@Getter
@Setter
public class TipoCatering extends Extra {

	@JsonManagedReference
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinTable(
			name = "sub_tipo_evento_tipo_catering",
			joinColumns = @JoinColumn(name = "tipo_catering_id"),
			inverseJoinColumns = @JoinColumn(name = "sub_tipo_evento_id"))
	private Set<SubTipoEvento> listaSubTipoEvento;
	
	@JsonBackReference
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tipoCatering")
    private List<PrecioConFechaTipoCatering> listaPrecioConFecha;

}
