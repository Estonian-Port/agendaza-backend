package com.estonianport.agendaza.model;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SubTipoEvento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String nombre;

	@ManyToOne
	@JoinColumn(name = "tipo_evento_id")
	private TipoEvento tipoEvento;

	@ManyToOne
	@JoinColumn(name = "capacidad_id")
	private Capacidad capacidad;

	@Column
	private LocalTime duracion;

	@Column(name = "cant_personal")
	private int cantPersonal;
	
	@Column(name = "valor_fin_semana")
	private int valorFinSemana;

	@Column(name = "horario_final_automatico")
	private boolean horarioFinalAutomatico;

	@JsonBackReference
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "subTipoEvento")
    private List<PrecioConFechaSubTipoEvento> listaPrecioConFecha;

	@JsonBackReference
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "listaSubTipoEvento")
	private Set<Servicio> listaServicio;

	@JsonBackReference
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "listaSubTipoEvento")
	private Set<ExtraSubTipoEvento> listaExtraSubTipoEvento;
	
	@JsonBackReference
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "listaSubTipoEvento")
	private Set<ExtraVariableSubTipoEvento> listaExtraVariableSubTipoEvento;

	@JsonBackReference
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "listaSubTipoEvento")
	private Set<ExtraVariableCatering> listaExtraVariableCatering;

	@JsonBackReference
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "listaSubTipoEvento")
	private Set<TipoCatering> listaTipoCatering;

}
