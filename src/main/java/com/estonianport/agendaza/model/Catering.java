package com.estonianport.agendaza.model;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Catering {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private int presupuesto;

	@Column(name = "catering_otro")
	private int canteringOtro;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "catering_tipo_catering",
			joinColumns = @JoinColumn(name = "catering_id"),
			inverseJoinColumns = @JoinColumn(name = "tipo_catering_id"))
	private Set<TipoCatering> listaTipoCatering;

    @OneToMany(mappedBy = "catering", cascade = CascadeType.ALL)
    private Set<CateringExtraVariableCatering> listaCateringExtraVariableCatering;

}
