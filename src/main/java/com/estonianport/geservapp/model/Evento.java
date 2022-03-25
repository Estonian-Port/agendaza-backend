package com.estonianport.geservapp.model;

import java.sql.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class Evento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String nombre;

	@ManyToOne
	@JoinColumn(name = "salon_id")
	private Salon salon;

	@ManyToOne
	@JoinColumn(name = "tipo_evento_id")
	private TipoEvento tipoEvento;

	@ManyToOne
	@JoinColumn(name = "sub_tipo_evento_id")
	private SubTipoEvento subTipoEvento;

	@OneToMany(targetEntity = EventoExtra.class, cascade = CascadeType.ALL)
	@JoinColumn(name = "evento_id", referencedColumnName = "id")
	private Set<EventoExtra> EventoExtra;

	@Column
	private Date start_date;

	@Column
	private Date end_date;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Salon getSalon() {
		return salon;
	}

	public void setSalon(Salon salon) {
		this.salon = salon;
	}

	public TipoEvento getTipoEvento() {
		return tipoEvento;
	}

	public void setTipoEvento(TipoEvento tipoEvento) {
		this.tipoEvento = tipoEvento;
	}

	public SubTipoEvento getSubTipoEvento() {
		return subTipoEvento;
	}

	public void setSubTipoEvento(SubTipoEvento subTipoEvento) {
		this.subTipoEvento = subTipoEvento;
	}

	public Date getStart_date() {
		return start_date;
	}

	public void setStart_date(Date start_date) {
		this.start_date = start_date;
	}

	public Date getEnd_date() {
		return end_date;
	}

	public void setEnd_date(Date end_date) {
		this.end_date = end_date;
	}

	public Set<EventoExtra> getEventoExtra() {
		return EventoExtra;
	}

	public void setEventoExtra(Set<EventoExtra> eventoExtra) {
		EventoExtra = eventoExtra;
	}

}
