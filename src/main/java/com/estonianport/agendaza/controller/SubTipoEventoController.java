package com.estonianport.agendaza.controller;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.estonianport.agendaza.commons.data.GeneralPath;
import com.estonianport.agendaza.commons.dateUtil.DateUtil;
import com.estonianport.agendaza.model.Capacidad;
import com.estonianport.agendaza.model.Salon;
import com.estonianport.agendaza.model.SubTipoEvento;
import com.estonianport.agendaza.model.TipoEvento;
import com.estonianport.agendaza.service.CapacidadService;
import com.estonianport.agendaza.service.SubTipoEventoService;
import com.estonianport.agendaza.service.TipoEventoService;

@Controller
public class SubTipoEventoController {

	@Autowired
	private SubTipoEventoService subTipoEventoService;

	@Autowired
	private TipoEventoService tipoEventoService;

	@Autowired
	private CapacidadService capacidadService;

	@RequestMapping("/abmSubTipoEvento")
	public String abm(Model model, HttpSession session) {
		model.addAttribute("listaSubTipoEvento", subTipoEventoService.getAll());

		// Salon en sesion para volver al calendario
		Salon salon = (Salon) session.getAttribute(GeneralPath.SALON);
		model.addAttribute(GeneralPath.SALON, salon);

		return GeneralPath.SUB_TIPO_EVENTO + GeneralPath.PATH_SEPARATOR + GeneralPath.ABM_SUB_TIPO_EVENTO;
	}

	@GetMapping("/saveSubTipoEvento/{id}")
	public String showSave(@PathVariable("id") Long id, Model model) {

		List<TipoEvento> listaTipoEvento = tipoEventoService.getAll();
		model.addAttribute("listaTipoEvento", listaTipoEvento);

		// Agrega lista de Hora
		model.addAttribute("listaHora", DateUtil.HORAS);

		// Agrega lista de Minuto
		model.addAttribute("listaMinuto", DateUtil.MINUTOS);

		if(id != null && id != 0) {
			model.addAttribute("subTipoEvento", subTipoEventoService.get(id));
		}else {
			model.addAttribute("subTipoEvento", new SubTipoEvento());
		}
		return GeneralPath.SUB_TIPO_EVENTO + GeneralPath.PATH_SEPARATOR + GeneralPath.SAVE_SUB_TIPO_EVENTO;
	}

	@PostMapping("/saveSubTipoEvento")
	public String save(SubTipoEvento subTipoEvento, Model model) {

		List<Capacidad> listaCapacidad = capacidadService.getAll();
		for(Capacidad capacidad : listaCapacidad) {
			if(capacidad.getCapacidadAdultos() == subTipoEvento.getCapacidad().getCapacidadAdultos() && capacidad.getCapacidadNinos() == subTipoEvento.getCapacidad().getCapacidadNinos()) {
				subTipoEvento.setCapacidad(capacidad);
			}
		}

		subTipoEventoService.save(subTipoEvento);

		return GeneralPath.REDIRECT + GeneralPath.ABM_SUB_TIPO_EVENTO;
	}

	@GetMapping("/deleteSubTipoEvento/{id}")
	public String delete(@PathVariable("id") Long id, Model model) {
		subTipoEventoService.delete(id);
		return GeneralPath.REDIRECT + GeneralPath.ABM_SUB_TIPO_EVENTO;
	}
}
