package com.estonianport.geservapp.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.estonianport.geservapp.commons.CodeGenerator;
import com.estonianport.geservapp.commons.EmailService;
import  com.estonianport.geservapp.commons.GeneralPath;
import com.estonianport.geservapp.commons.ItextService;
import com.estonianport.geservapp.container.ReservaContainer;
import com.estonianport.geservapp.model.Evento;
import com.estonianport.geservapp.model.EventoExtra;
import com.estonianport.geservapp.model.Extra;
import com.estonianport.geservapp.model.Salon;
import com.estonianport.geservapp.model.Sexo;
import com.estonianport.geservapp.model.SubTipoEvento;
import com.estonianport.geservapp.model.TipoEvento;
import com.estonianport.geservapp.service.ClienteService;
import com.estonianport.geservapp.service.EventoService;
import com.estonianport.geservapp.service.ExtraService;
import com.estonianport.geservapp.service.SexoService;
import com.estonianport.geservapp.service.SubTipoEventoService;
import com.estonianport.geservapp.service.TipoEventoService;
import com.estonianport.geservapp.service.UsuarioService;

@Controller
public class ReservaController {

	@Autowired
	private EventoService eventoService;

	@Autowired
	private TipoEventoService tipoEventoService;

	@Autowired
	private SubTipoEventoService subTipoEventoService;

	@Autowired
	private ExtraService extraService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private UsuarioService usuarioService;

	@Autowired
	private SexoService sexoService;

	@Autowired
	private ClienteService clienteService;

	@GetMapping("/saveEvento/{id}")
	public String showSave(@PathVariable("id") Long id, Model model, HttpSession session) {

		// Salon en sesion para volver al calendario
		Salon salon = (Salon) session.getAttribute(GeneralPath.SALON);
		model.addAttribute(GeneralPath.SALON, salon);

		// Agrega lista Extras
		List<Extra> listaExtra = extraService.getAll();
		model.addAttribute("listaExtra", listaExtra);

		List<Sexo> listaSexo = sexoService.getAll();
		model.addAttribute("listaSexo", listaSexo);

		if(id != null && id != 0) {
			Evento evento = eventoService.get(id);

			// Setea la lista de extras que tiene el evento seleccionadas
			List<Extra> listaExtraSeleccionadas =  new ArrayList<Extra>();
			Set<EventoExtra> listaEventoExtra = evento.getEventoExtra();
			for(EventoExtra eventoExtra : listaEventoExtra) {
				listaExtraSeleccionadas.add(eventoExtra.getExtra());
			}

			ReservaContainer reservaContainer = new ReservaContainer();
			reservaContainer.setEvento(evento);

			// Setea la hora y fecha del evento
			String fecha = evento.getStart_date().getDayOfMonth() + "-" + evento.getStart_date().getMonth().getValue() + "-" + evento.getStart_date().getYear();
			reservaContainer.setFecha(fecha);
			String horaInicio = String.valueOf(evento.getStart_date().getHour()) + ":" +String.valueOf(evento.getStart_date().getMinute());
			reservaContainer.setInicio(horaInicio);
			String horaFin = String.valueOf(evento.getEnd_date().getHour()) + ":" +String.valueOf(evento.getEnd_date().getMinute());
			reservaContainer.setFin(horaFin);

			model.addAttribute("listaExtraSeleccionadas", listaExtraSeleccionadas);
			model.addAttribute("reservaContainer", reservaContainer);

			model.addAttribute("volver", "../" + GeneralPath.ABM_EVENTO + GeneralPath.PATH_SEPARATOR + salon.getId());
			return GeneralPath.EVENTO + GeneralPath.PATH_SEPARATOR + GeneralPath.EDIT_EVENTO;
		}else {

			// Agrega lista de Tipo Eventos
			List<TipoEvento> listaTipoEvento = tipoEventoService.getAll();
			model.addAttribute("listaTipoEvento", listaTipoEvento);

			// Agrega lista de Sub Tipo Eventos
			List<SubTipoEvento> listaSubTipoEvento = subTipoEventoService.getAll();
			model.addAttribute("listaSubTipoEvento", listaSubTipoEvento);

			// Crea una instancia de EventoExtraContainer para agregar todos los Extra en la vista
			ReservaContainer reservaContainer = new ReservaContainer();
			reservaContainer.setExtra(new ArrayList<>());
			for(Extra extra : listaExtra) {
				reservaContainer.getExtra().add(extra);
			}
			model.addAttribute("reservaContainer", reservaContainer);
			return GeneralPath.EVENTO + GeneralPath.PATH_SEPARATOR + GeneralPath.SAVE_EVENTO;
		}
	}

	@PostMapping("/saveEvento")
	public String save(@ModelAttribute("reservaContainer") ReservaContainer reservaContainer, Model model, HttpSession session, Authentication authentication) {

		// El container retorna los objetos a usar
		Evento evento = reservaContainer.getEvento();
		List<Extra> listaExtra = reservaContainer.getExtra();

		// Salon en sesion para volver al calendario y setear en el save
		Salon salon = (Salon) session.getAttribute(GeneralPath.SALON);
		evento.setSalon(salon);

		// Crea el Set EventoExtra para agregar los Extra y luego a Evento
		Set<EventoExtra> setEventoExtra = new HashSet<EventoExtra>();
		EventoExtra eventoExtra = null;

		// Setea la hora y fecha del evento
		try{
			evento.setStart_date(LocalDateTime.parse(reservaContainer.getFecha() + " " + reservaContainer.getInicio(), DateTimeFormatter.ofPattern("dd-M-yyyy HH:mm")));
		}catch(Exception e){
			evento.setStart_date(LocalDateTime.parse(reservaContainer.getFecha() + " " + reservaContainer.getInicio(), DateTimeFormatter.ofPattern("dd-M-yyyy HH:m")));
		}

		// Setea usuario que genero la reserva
		evento.setUsuario(usuarioService.findUserByUsername(authentication.getName()));

		if(evento.getCodigo() == null || evento.getCodigo() == "" ){
			// Crea el codigo del evento
			evento.setCodigo(CodeGenerator.GetBase26Only4Letters());
		}

		// Chequea si el evento es toda la noche, en vaso de serlo le setea una fecha de final 1 dia despues y a las 5am
		if(!reservaContainer.getHastaElOtroDia()) {
			try {
				evento.setEnd_date(LocalDateTime.parse(reservaContainer.getFecha() + " " + reservaContainer.getFin(), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
			}catch(Exception e){
				evento.setEnd_date(LocalDateTime.parse(reservaContainer.getFecha() + " " + reservaContainer.getFin(), DateTimeFormatter.ofPattern("dd-M-yyyy HH:m")));
			}
		}else {
			LocalDateTime fechaFin = null;
			try {
				fechaFin = LocalDateTime.parse(reservaContainer.getFecha() + " " + "05:00", DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
			}catch(Exception e){
				fechaFin = LocalDateTime.parse(reservaContainer.getFecha() + " " + "05:00", DateTimeFormatter.ofPattern("dd-M-yyyy HH:m"));
			}
			evento.setEnd_date(fechaFin.plusDays(1));
		}

		// Guarda el cliente en la base de datos
		clienteService.save(reservaContainer.getCliente());
		evento.setCliente(clienteService.get(reservaContainer.getCliente().getId()));

		// Guarda el Set EventoExtra que contiene los Extras seleccionados en la vista
		if(listaExtra != null && !listaExtra.isEmpty()) {
			for(Extra extra : listaExtra) {
				eventoExtra = new EventoExtra();
				eventoExtra.setExtra(extra);
				setEventoExtra.add(eventoExtra);
			}
		}

		evento.setEventoExtra(setEventoExtra);
		eventoService.save(evento);

		// Agrega todo el objeto TipoEvento y SubTipoEvento para envio de mail y pdf
		evento.setTipoEvento(tipoEventoService.get(evento.getTipoEvento().getId()));
		evento.setSubTipoEvento(subTipoEventoService.get(evento.getSubTipoEvento().getId()));

		// Envia mail con comprobante
		emailService.enviarMailComprabanteReserva(evento, listaExtra);

		return GeneralPath.REDIRECT + GeneralPath.ABM_EVENTO + GeneralPath.PATH_SEPARATOR + salon.getId();
	}
}
