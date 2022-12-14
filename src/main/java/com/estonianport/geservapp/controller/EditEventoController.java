package com.estonianport.geservapp.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.estonianport.geservapp.commons.data.GeneralPath;
import com.estonianport.geservapp.commons.dateUtil.DateUtil;
import com.estonianport.geservapp.commons.emailService.EmailService;
import com.estonianport.geservapp.container.CodigoContainer;
import com.estonianport.geservapp.container.ExtraContainer;
import com.estonianport.geservapp.container.ExtraVariableContainer;
import com.estonianport.geservapp.container.ReservaContainer;
import com.estonianport.geservapp.model.Catering;
import com.estonianport.geservapp.model.CateringExtraVariableCatering;
import com.estonianport.geservapp.model.Evento;
import com.estonianport.geservapp.model.EventoExtraVariableSubTipoEvento;
import com.estonianport.geservapp.model.ExtraSubTipoEvento;
import com.estonianport.geservapp.model.ExtraVariableCatering;
import com.estonianport.geservapp.model.ExtraVariableSubTipoEvento;
import com.estonianport.geservapp.model.Pago;
import com.estonianport.geservapp.model.PrecioConFecha;
import com.estonianport.geservapp.model.PrecioConFechaExtraSubTipoEvento;
import com.estonianport.geservapp.model.PrecioConFechaExtraVariableCatering;
import com.estonianport.geservapp.model.PrecioConFechaExtraVariableSubTipoEvento;
import com.estonianport.geservapp.model.PrecioConFechaSubTipoEvento;
import com.estonianport.geservapp.model.PrecioConFechaTipoCatering;
import com.estonianport.geservapp.model.Salon;
import com.estonianport.geservapp.model.SubTipoEvento;
import com.estonianport.geservapp.model.TipoCatering;
import com.estonianport.geservapp.service.CateringExtraVariableCateringService;
import com.estonianport.geservapp.service.CateringService;
import com.estonianport.geservapp.service.EventoExtraVariableSubTipoEventoService;
import com.estonianport.geservapp.service.EventoService;
import com.estonianport.geservapp.service.PagoService;
import com.estonianport.geservapp.service.SubTipoEventoService;

@Controller
public class EditEventoController {

	@Autowired
	private EventoService eventoService;

	@Autowired
	private CateringService cateringService;

	@Autowired
	private SubTipoEventoService subTipoEventoService;

	@Autowired
	private CateringExtraVariableCateringService cateringExtraVariableCateringService;

	@Autowired
	private EventoExtraVariableSubTipoEventoService eventoExtraVariableSubTipoEventoService;

	@Autowired
	private PagoService pagoService;

	@Autowired
	private EmailService emailService;

	// TODO Refactor para no tener que setear el null en cada una de los extras por subTipoEvento
	@GetMapping("/saveEventoExtra/{id}")
	public String showSaveExtra(@PathVariable("id") Long id, Model model, HttpSession session) {

		// Salon en sesion para volver al calendario
		Salon salon = (Salon) session.getAttribute(GeneralPath.SALON);
		model.addAttribute(GeneralPath.SALON, salon);

		ReservaContainer reservaContainer = new ReservaContainer();

		Evento evento = eventoService.get(id);

		reservaContainer.setEvento(evento);

		model.addAttribute("reservaContainer", reservaContainer);

		// ------------------------- Extra ------------------------------

		// Agrega lista Extras
		Set<ExtraSubTipoEvento> listaExtra = evento.getSubTipoEvento().getListaExtraSubTipoEvento();
		List<ExtraContainer> listaExtraContainer = new ArrayList<ExtraContainer>();

		for(ExtraSubTipoEvento extra : listaExtra) {
			extra.setListaSubTipoEvento(null);
			
			for(PrecioConFecha PrecioConFecha : extra.getListaPrecioConFecha()) {
				ExtraContainer extraContainer = getExtra(evento.getStartd(), PrecioConFecha, salon, extra.getId(), extra.getNombre());
				if(extraContainer != null) {
					listaExtraContainer.add(extraContainer);
				}
			}
		}

		model.addAttribute("listaExtra", listaExtraContainer);

		// Agrega lista extra seleccionadas
		Set<ExtraSubTipoEvento> listaExtraSeleccionadas = evento.getListaExtraSubTipoEvento();

		for(ExtraSubTipoEvento extraSubTipoEvento : listaExtraSeleccionadas) {
			extraSubTipoEvento.setListaSubTipoEvento(null);
		}

		model.addAttribute("listaExtraSeleccionadas", listaExtraSeleccionadas);


		// ------------------------- Extra variable ------------------------------
		Set<ExtraVariableSubTipoEvento> listaExtraVariable = evento.getSubTipoEvento().getListaExtraVariableSubTipoEvento();
		List<ExtraContainer> listaExtraVariableContainer = new ArrayList<ExtraContainer>();

		for(ExtraVariableSubTipoEvento extra : listaExtraVariable){
			extra.setListaSubTipoEvento(null);

			for(PrecioConFecha PrecioConFecha : extra.getListaPrecioConFecha()) {
				ExtraContainer extraContainer = getExtra(evento.getStartd(), PrecioConFecha, salon, extra.getId(), extra.getNombre());
				if(extraContainer != null) {
					listaExtraVariableContainer.add(extraContainer);
				}
			}
		}

		// Agrega lista Extras Variables
		model.addAttribute("listaExtraVariable", listaExtraVariableContainer);

		// Agrega lista extra seleccionadas
		Set<EventoExtraVariableSubTipoEvento> listaEventoExtraVariableSeleccionadas = evento.getListaEventoExtraVariable();

		for(EventoExtraVariableSubTipoEvento EventoExtraVariableSubTipoEvento : listaEventoExtraVariableSeleccionadas) {
			EventoExtraVariableSubTipoEvento.getExtraVariableSubTipoEvento().setListaSubTipoEvento(null);
			EventoExtraVariableSubTipoEvento.setEvento(null);
		}

		model.addAttribute("listaExtraVariableSeleccionadas", listaEventoExtraVariableSeleccionadas);

		// ------------------------- Precio subTipoEvento en fecha  ------------------------------
		int presupuesto = 0;
		SubTipoEvento subTipoEvento = subTipoEventoService.get(evento.getSubTipoEvento().getId());
		LocalDateTime fechaEvento = evento.getStartd();

		List<PrecioConFechaSubTipoEvento> listaPrecioConFecha = subTipoEvento.getListaPrecioConFecha();

		for(PrecioConFecha precioConFecha : listaPrecioConFecha) {

			if(fechaEvento.getYear() == precioConFecha.getDesde().getYear()) {
				List<Integer> rangoMeses = IntStream.range(precioConFecha.getDesde().getMonthValue(), precioConFecha.getHasta().getMonthValue() + 1).boxed().collect(Collectors.toList());

				if(rangoMeses.contains(fechaEvento.getMonthValue()) && precioConFecha.getSalon().getId() == salon.getId()){
					if(DateUtil.isFinDeSemana(fechaEvento)) {
						presupuesto = precioConFecha.getPrecio() + subTipoEvento.getValorFinSemana();
					}else {
						presupuesto = precioConFecha.getPrecio();
					}
				}
			}
		}

		model.addAttribute("presupuesto", presupuesto);
		
		// Setea el boton volver y el codigo para que pueda encontrar el evento al volver
		model.addAttribute(GeneralPath.VOLVER, "../" + "eventoVolver");
		session.setAttribute("codigoEvento", evento.getCodigo());
		
		return GeneralPath.EVENTO + GeneralPath.PATH_SEPARATOR + "editEventoExtra";
	}

	@PostMapping("/saveEventoExtra")
	public String saveEventoExtra(@ModelAttribute("reservaContainer") ReservaContainer reservaContainer, Model model, HttpSession session) {

		// El container retorna los objetos a usar
		Evento evento =  eventoService.get(reservaContainer.getEvento().getId());

		// Borra todos los cateringExtraVariable anteriores
		List<EventoExtraVariableSubTipoEvento> eventoExtraVariableSubTipoEventoByEvento = eventoExtraVariableSubTipoEventoService.getEventoExtraVariableSubTipoEventoByEvento(evento);
		for(EventoExtraVariableSubTipoEvento eventoExtraVariableSubTipoEvento : eventoExtraVariableSubTipoEventoByEvento) {
			eventoExtraVariableSubTipoEventoService.delete(eventoExtraVariableSubTipoEvento.getId());
		}

		// Comprueba que la lista de extras variables no sea null
		if(reservaContainer.getEventoExtraVariableSubTipoEvento() != null) {

			// Elimina los extras variables con cantidad 0
			List<EventoExtraVariableSubTipoEvento> listEventoExtraVariableSubTipoEvento = reservaContainer.getEventoExtraVariableSubTipoEvento();
			listEventoExtraVariableSubTipoEvento.removeIf(n -> n.getCantidad() == 0);

			// Setea el catering a cada uno de los ExtraVariableCatering
			listEventoExtraVariableSubTipoEvento.stream().forEach(extraVariable -> extraVariable.setEvento(evento));

			for(EventoExtraVariableSubTipoEvento eventoExtraVariableSubTipoEvento : listEventoExtraVariableSubTipoEvento) {
				eventoExtraVariableSubTipoEventoService.save(eventoExtraVariableSubTipoEvento);
			}
		}

		evento.setListaExtraSubTipoEvento(reservaContainer.getExtraSubTipoEvento());

		// Salon en sesion para volver al calendario y setear en el save
		Salon salon = (Salon) session.getAttribute(GeneralPath.SALON);

		// Guarda el evento en la base de datos
		eventoService.save(evento);

		// Envia mail con comprobante
		emailService.enviarMailComprabanteReserva(reservaContainer, "modificado los extras");

		return GeneralPath.REDIRECT + GeneralPath.ABM_EVENTO + GeneralPath.PATH_SEPARATOR + salon.getId();
	}

	@GetMapping("/saveEventoHora/{id}")
	public String showSaveHora(@PathVariable("id") Long id, Model model, HttpSession session) {

		// Salon en sesion para volver al calendario
		Salon salon = (Salon) session.getAttribute(GeneralPath.SALON);
		model.addAttribute(GeneralPath.SALON, salon);

		// Agrega lista de Hora
		model.addAttribute("listaHora", DateUtil.HORAS);

		// Agrega lista de Minuto
		model.addAttribute("listaMinuto", DateUtil.MINUTOS);

		ReservaContainer reservaContainer = new ReservaContainer();

		Evento evento = eventoService.get(id);

		reservaContainer.setEvento(evento);

		// Setea fecha del evento
		reservaContainer.setFecha(DateUtil.getFecha(evento.getStartd()));

		// Setea hora de inicio
		reservaContainer.setInicio(DateUtil.getHorario(evento.getStartd()));

		// Setea hora de fin
		reservaContainer.setFin(DateUtil.getHorario(evento.getEndd()));

		// Setea checkbox hastaElotroDia en caso de que el dia de caundo termina del evento es 1 mas que cuando empieza
		if(evento.getStartd().plusDays(1).getDayOfMonth() == evento.getEndd().getDayOfMonth()) {
			reservaContainer.setHastaElOtroDia(true);
		}

		model.addAttribute("reservaContainer", reservaContainer);

		// Setea el boton volver y el codigo para que pueda encontrar el evento al volver
		model.addAttribute("volver", "../" + "eventoVolver");
		session.setAttribute("codigoEvento", evento.getCodigo());

		return GeneralPath.EVENTO + GeneralPath.PATH_SEPARATOR + "editEventoHora";
	}


	@PostMapping("/saveEventoHora")
	public String saveEventoHora(@ModelAttribute("reservaContainer") ReservaContainer reservaContainer, Model model, HttpSession session) {

		// El container retorna los objetos a usar
		Evento evento =  eventoService.get(reservaContainer.getEvento().getId());

		// Salon en sesion para volver al calendario y setear en el save
		Salon salon = (Salon) session.getAttribute(GeneralPath.SALON);

		// Setea fecha del evento y hora de inicio
		evento.setStartd(DateUtil.createFechaConHora(reservaContainer.getFecha(), reservaContainer.getInicio()));

		// Chequea si el evento es toda la noche, en caso de serlo le setea una fecha de final 1 dia despues
		if(!reservaContainer.getHastaElOtroDia()) {
			evento.setEndd(DateUtil.createFechaConHora(reservaContainer.getFecha(), reservaContainer.getFin()));
		}else {
			evento.setEndd(DateUtil.createFechaConHora(reservaContainer.getFecha(), reservaContainer.getFin()).plusDays(1));
		}

		// Guarda el evento en la base de datos
		eventoService.save(evento);

		// Envia mail con comprobante
		emailService.enviarMailComprabanteReserva(reservaContainer, "modificado el horario");

		return GeneralPath.REDIRECT + GeneralPath.ABM_EVENTO + GeneralPath.PATH_SEPARATOR + salon.getId();
	}

	@GetMapping("/saveEventoCatering/{id}")
	public String showSaveCatering(@PathVariable("id") Long id, Model model, HttpSession session) {

		// Salon en sesion para volver al calendario
		Salon salon = (Salon) session.getAttribute(GeneralPath.SALON);
		model.addAttribute(GeneralPath.SALON, salon);

		Evento evento = eventoService.get(id);

		ReservaContainer reservaContainer = new ReservaContainer();
		reservaContainer.setEvento(evento);

		Set<ExtraVariableCatering> listaExtraVariableCatering = evento.getSubTipoEvento().getListaExtraVariableCatering();
		List<ExtraContainer> listaExtraVariableCateringContainer = new ArrayList<ExtraContainer>();

		for(ExtraVariableCatering extra : listaExtraVariableCatering) {
			extra.setListaSubTipoEvento(null);
			
			for(PrecioConFecha PrecioConFecha : extra.getListaPrecioConFecha()) {
				ExtraContainer extraContainer = getExtra(evento.getStartd(), PrecioConFecha, salon, extra.getId(), extra.getNombre());
				if(extraContainer != null) {
					listaExtraVariableCateringContainer.add(extraContainer);
				}
			}
		}

		model.addAttribute("listaExtraCatering", listaExtraVariableCateringContainer);
		
		Set<CateringExtraVariableCatering> listaCateringExtraCateringSeleccionadas = evento.getCatering().getListaCateringExtraVariableCatering();

		for(CateringExtraVariableCatering cateringExtraVariableCatering : listaCateringExtraCateringSeleccionadas) {
			cateringExtraVariableCatering.getExtraVariableCatering().setListaSubTipoEvento(null);
			cateringExtraVariableCatering.setCatering(null);
		}

		model.addAttribute("listaExtraCateringSeleccionadas", listaCateringExtraCateringSeleccionadas);

		Set<TipoCatering> listaTipoCatering = evento.getSubTipoEvento().getListaTipoCatering();
		List<ExtraContainer> listaTipoCateringSeleccionadasContainer = new ArrayList<ExtraContainer>();

		for(TipoCatering tipoCatering : listaTipoCatering) {
			tipoCatering.setListaSubTipoEvento(null);
	
			for(PrecioConFecha PrecioConFecha : tipoCatering.getListaPrecioConFecha()) {
				ExtraContainer extraContainer = getExtra(evento.getStartd(), PrecioConFecha, salon, tipoCatering.getId(), tipoCatering.getNombre());
				if(extraContainer != null) {
					listaTipoCateringSeleccionadasContainer.add(extraContainer);
				}
			}
		}

		model.addAttribute("listaTipoCatering", listaTipoCateringSeleccionadasContainer);

		Set<TipoCatering> listaTipoCateringSeleccionadas = evento.getCatering().getListaTipoCatering();

		for(TipoCatering tipoCatering : listaTipoCateringSeleccionadas) {
			tipoCatering.setListaSubTipoEvento(null);

		}

		model.addAttribute("listaTipoCateringSeleccionadas", listaTipoCateringSeleccionadas);

		model.addAttribute("reservaContainer", reservaContainer);

		// Setea el boton volver y el codigo para que pueda encontrar el evento al volver
		model.addAttribute(GeneralPath.VOLVER, "../" + "eventoVolver");
		session.setAttribute("codigoEvento", evento.getCodigo());

		return GeneralPath.EVENTO + GeneralPath.PATH_SEPARATOR + "editEventoCatering";
	}

	@PostMapping("/saveEventoCatering")
	public String saveEventoCatering(@ModelAttribute("reservaContainer") ReservaContainer reservaContainer, Model model, HttpSession session) {

		// El container retorna los objetos a usar
		Evento evento =  eventoService.get(reservaContainer.getEvento().getId());

		// Salon en sesion para volver al calendario y setear en el save
		Salon salon = (Salon) session.getAttribute(GeneralPath.SALON);

		// Setea el presupuesto y catering otro
		Catering catering = reservaContainer.getEvento().getCatering();

		catering.setId(evento.getCatering().getId());

		// Borra todos los cateringExtraVariable anteriores
		List<CateringExtraVariableCatering> cateringExtraVariableCateringByCatering = cateringExtraVariableCateringService.getCateringExtraVariableCateringByCatering(catering);
		for(CateringExtraVariableCatering cateringExtraVariableCatering : cateringExtraVariableCateringByCatering) {
			cateringExtraVariableCateringService.delete(cateringExtraVariableCatering.getId());
		}

		// Comprueba que la lista de extras variables no sea null
		if(reservaContainer.getCateringExtraVariableCatering() != null) {

			// Elimina los extras variables con cantidad 0
			List<CateringExtraVariableCatering> listExtraVariableCatering = reservaContainer.getCateringExtraVariableCatering();
			listExtraVariableCatering.removeIf(n -> n.getCantidad() == 0);

			// Setea el catering a cada uno de los ExtraVariableCatering
			listExtraVariableCatering.stream().forEach(cateringExtraVariable -> cateringExtraVariable.setCatering(catering));

			for(CateringExtraVariableCatering cateringExtraVariableCatering : listExtraVariableCatering) {
				cateringExtraVariableCateringService.save(cateringExtraVariableCatering);
			}
		}

		catering.setListaTipoCatering(reservaContainer.getTipoCatering());

		// Guarda el evento en la base de datos
		cateringService.save(catering);

		// Envia mail con comprobante
		emailService.enviarMailComprabanteReserva(reservaContainer, "modificado el catering");

		return GeneralPath.REDIRECT + GeneralPath.ABM_EVENTO + GeneralPath.PATH_SEPARATOR + salon.getId();
	}

	@GetMapping("/saveEventoPago/{id}")
	public String showSavePago(@PathVariable("id") Long id, Model model, HttpSession session) {

		// Salon en sesion para volver al calendario
		Salon salon = (Salon) session.getAttribute(GeneralPath.SALON);
		model.addAttribute(GeneralPath.SALON, salon);

		Evento evento = eventoService.get(id);
		evento.setSubTipoEvento(null);
		evento.setListaEventoExtraVariable(null);
		evento.setListaExtraSubTipoEvento(null);

		int presupuestoTotal = 0;

		if(evento.getPresupuesto() != 0) {
			presupuestoTotal += evento.getPresupuesto();
		}

		if(evento.getCatering() != null && evento.getCatering().getPresupuesto() != 0) {
			presupuestoTotal += evento.getCatering().getPresupuesto();
		}

		model.addAttribute("presupuestoTotal", presupuestoTotal);

		evento.setCatering(null);

		ReservaContainer reservaContainer = new ReservaContainer();
		reservaContainer.setEvento(evento);

		model.addAttribute("reservaContainer", reservaContainer);

		List<Pago> listaPagos = pagoService.findPagosByEvento(evento);

		for(Pago pago : listaPagos) {
			pago.setEvento(null);
		}

		model.addAttribute("listaPagos", listaPagos);


		CodigoContainer codigoContainer = new CodigoContainer();
		codigoContainer.setCodigo(evento.getCodigo());

		model.addAttribute("codigoContainer", codigoContainer);	

		// Setea el boton volver y el codigo para que pueda encontrar el evento al volver
		model.addAttribute(GeneralPath.VOLVER, "../" + "eventoVolver");
		session.setAttribute("codigoEvento", evento.getCodigo());
		
		// Setea el valor de volver para el boton volver de savePago
		session.setAttribute(GeneralPath.VOLVER, "../saveEventoPago/" + evento.getId());

		// Setea el valor de volver cuando termine de guardar
		session.setAttribute(GeneralPath.VOLVER + GeneralPath.ACTION, "saveEventoPago/" + evento.getId());

		return GeneralPath.EVENTO + GeneralPath.PATH_SEPARATOR + "editEventoPago";
	}

	@GetMapping("/verEvento/{id}")
	public String verEvento(@PathVariable("id") Long id, Model model, HttpSession session) {

		// Salon en sesion para volver al calendario
		Salon salon = (Salon) session.getAttribute(GeneralPath.SALON);
		model.addAttribute(GeneralPath.SALON, salon);

		Evento evento = eventoService.get(id);

		SubTipoEvento subTipoEvento = new SubTipoEvento();

		subTipoEvento.setNombre(evento.getSubTipoEvento().getNombre());
		evento.setSubTipoEvento(subTipoEvento);

		//------------------------ Presupuesto ----------------
		int presupuesto = 0;

		if(evento.getPresupuesto() != 0) {
			presupuesto += evento.getPresupuesto();
		}

		if(evento.getCatering() != null && evento.getCatering().getPresupuesto() != 0) {
			presupuesto += evento.getCatering().getPresupuesto();
		}
		
		model.addAttribute("presupuesto", presupuesto);
		
		// -------------------------- Extras ---------------------------------------
		Set<ExtraSubTipoEvento> listaExtra = evento.getListaExtraSubTipoEvento();
		Set<ExtraContainer> listaExtraSeleccionadas = new HashSet<ExtraContainer>();

		for(ExtraSubTipoEvento extra : listaExtra) {
			List<PrecioConFechaExtraSubTipoEvento> listaPrecioConFechaExtraVariable =  extra.getListaPrecioConFecha();
			List<PrecioConFecha> listaPrecioConFecha =  new ArrayList<PrecioConFecha>();

			for(PrecioConFecha precioConFecha : listaPrecioConFechaExtraVariable) {
				listaPrecioConFecha.add(precioConFecha);
			}
			
			listaExtraSeleccionadas.add(this.getExtra(evento.getStartd(), listaPrecioConFecha, salon, extra.getId(), extra.getNombre()));
		}

		model.addAttribute("listaExtraSeleccionadas", listaExtraSeleccionadas);
		// ---------------------------------------------------------------------------

		// -------------------------- Extras Variables -------------------------------
		Set<EventoExtraVariableSubTipoEvento> listaEventoExtraVariableSeleccionadas = evento.getListaEventoExtraVariable();
		Set<ExtraVariableContainer> listaExtraVariableSeleccionadas = new HashSet<ExtraVariableContainer>();

		for(EventoExtraVariableSubTipoEvento eventoExtraVariableSubTipoEvento : listaEventoExtraVariableSeleccionadas) {
			ExtraVariableSubTipoEvento extraVariable = eventoExtraVariableSubTipoEvento.getExtraVariableSubTipoEvento();
			
			List<PrecioConFechaExtraVariableSubTipoEvento> listaPrecioConFechaExtraVariable =  extraVariable.getListaPrecioConFecha();
			List<PrecioConFecha> listaPrecioConFecha =  new ArrayList<PrecioConFecha>();

			for(PrecioConFecha precioConFecha : listaPrecioConFechaExtraVariable) {
				listaPrecioConFecha.add(precioConFecha);
			}
			
			listaExtraVariableSeleccionadas.add(this.getExtraVariable(evento.getStartd(), listaPrecioConFecha, salon, extraVariable.getId(), extraVariable.getNombre(), eventoExtraVariableSubTipoEvento.getCantidad()));
		}

		model.addAttribute("listaExtraVariableSeleccionadas", listaExtraVariableSeleccionadas);
		// ------------------------------------------------------------------------------

		// -------------------------- Extras Catering -------------------------------
		Set<CateringExtraVariableCatering> listaCateringExtraCateringSeleccionadas = evento.getCatering().getListaCateringExtraVariableCatering();
		Set<ExtraVariableContainer> listaExtraCateringSeleccionadas = new HashSet<ExtraVariableContainer>();

		for(CateringExtraVariableCatering cateringExtraVariableCatering : listaCateringExtraCateringSeleccionadas) {
			ExtraVariableCatering extraVariable = cateringExtraVariableCatering.getExtraVariableCatering();
			
			List<PrecioConFechaExtraVariableCatering> listaPrecioConFechaExtraVariable =  extraVariable.getListaPrecioConFecha();
			List<PrecioConFecha> listaPrecioConFecha =  new ArrayList<PrecioConFecha>();

			for(PrecioConFecha precioConFecha : listaPrecioConFechaExtraVariable) {
				listaPrecioConFecha.add(precioConFecha);
			}
			
			listaExtraCateringSeleccionadas.add(this.getExtraVariable(evento.getStartd(), listaPrecioConFecha, salon, extraVariable.getId(), extraVariable.getNombre(), cateringExtraVariableCatering.getCantidad()));
		}

		model.addAttribute("listaExtraCateringSeleccionadas", listaExtraCateringSeleccionadas);
		// ------------------------------------------------------------------------------
		
		// -------------------------- Tipo Catering ---------------------------------------
		Set<TipoCatering> listaTipoCatering = evento.getCatering().getListaTipoCatering();
		Set<ExtraContainer> listaTipoCateringSeleccionadas = new HashSet<ExtraContainer>();

		for(TipoCatering tipoCatering : listaTipoCatering) {
			List<PrecioConFechaTipoCatering> listaPrecioConFechaExtraVariable =  tipoCatering.getListaPrecioConFecha();
			List<PrecioConFecha> listaPrecioConFecha =  new ArrayList<PrecioConFecha>();

			for(PrecioConFecha precioConFecha : listaPrecioConFechaExtraVariable) {
				listaPrecioConFecha.add(precioConFecha);
			}
			
			listaTipoCateringSeleccionadas.add(this.getExtra(evento.getStartd(), listaPrecioConFecha, salon, tipoCatering.getId(), tipoCatering.getNombre()));
		}

		model.addAttribute("listaTipoCateringSeleccionadas", listaTipoCateringSeleccionadas);
		// ---------------------------------------------------------------------------
		
		evento.setListaEventoExtraVariable(null);
		evento.setListaExtraSubTipoEvento(null);
		
		evento.getCatering().setListaCateringExtraVariableCatering(null);
		evento.getCatering().setListaTipoCatering(null);

		ReservaContainer reservaContainer = new ReservaContainer();

		// ----------------- Hora ------------------------
		// Agrega lista de Hora
		model.addAttribute("listaHora", DateUtil.HORAS);

		// Agrega lista de Minuto
		model.addAttribute("listaMinuto", DateUtil.MINUTOS);

		// Setea fecha del evento
		reservaContainer.setFecha(DateUtil.getFecha(evento.getStartd()));

		// Setea hora de inicio
		reservaContainer.setInicio(DateUtil.getHorario(evento.getStartd()));

		// Setea hora de fin
		reservaContainer.setFin(DateUtil.getHorario(evento.getEndd()));

		reservaContainer.setEvento(evento);
		model.addAttribute("reservaContainer", reservaContainer);

		model.addAttribute("volver", "../" + GeneralPath.ABM_EVENTO + GeneralPath.PATH_SEPARATOR + salon.getId());
		
		// En caso de descargar comprobante

		CodigoContainer codigoContainer = new CodigoContainer();
		codigoContainer.setCodigo(evento.getCodigo());
		model.addAttribute("codigoContainer", codigoContainer);
		
		// Setea el boton volver y el codigo para que pueda encontrar el evento al volver
		model.addAttribute(GeneralPath.VOLVER, "../" + "eventoVolver");
		session.setAttribute("codigoEvento", evento.getCodigo());

		model.addAttribute("eventoEncontrado", session.getAttribute("eventoEncontrado"));
		session.setAttribute("eventoEncontrado", null);

		session.setAttribute("eventoId", evento.getId());

		return GeneralPath.EVENTO + GeneralPath.PATH_SEPARATOR + "verEvento";
	}
	
	private ExtraContainer getExtra(LocalDateTime fecha, PrecioConFecha precioConFecha, Salon salon, Long extraId, String extraNombre) {
		
		if(fecha.getYear() == precioConFecha.getDesde().getYear()) {
			List<Integer> rangoMeses = IntStream.range(precioConFecha.getDesde().getMonthValue(), precioConFecha.getHasta().getMonthValue() + 1).boxed().collect(Collectors.toList());

			if(rangoMeses.contains(fecha.getMonthValue()) && precioConFecha.getSalon().getId() == salon.getId()){
				return new ExtraContainer(extraId, extraNombre, precioConFecha.getPrecio());
			}
		}
		return null;
	}
	
	private ExtraContainer getExtra(LocalDateTime fecha, List<PrecioConFecha> listaPrecioConFecha, Salon salon, Long extraId, String extraNombre) {
		
		for(PrecioConFecha precioConFecha : listaPrecioConFecha) {
			if(fecha.getYear() == precioConFecha.getDesde().getYear()) {
				List<Integer> rangoMeses = IntStream.range(precioConFecha.getDesde().getMonthValue(), precioConFecha.getHasta().getMonthValue() + 1).boxed().collect(Collectors.toList());
	
				if(rangoMeses.contains(fecha.getMonthValue()) && precioConFecha.getSalon().getId() == salon.getId()){
					return new ExtraContainer(extraId, extraNombre, precioConFecha.getPrecio());
				}
			}
		}
		return new ExtraContainer(extraId, extraNombre, 0);
	}
	
	private ExtraVariableContainer getExtraVariable(LocalDateTime fecha, List<PrecioConFecha> listaPrecioConFecha, Salon salon, Long extraId, String extraNombre, int extraCantidad) {
		
		for(PrecioConFecha precioConFecha : listaPrecioConFecha) {
			if(fecha.getYear() == precioConFecha.getDesde().getYear()) {
				List<Integer> rangoMeses = IntStream.range(precioConFecha.getDesde().getMonthValue(), precioConFecha.getHasta().getMonthValue() + 1).boxed().collect(Collectors.toList());
	
				if(rangoMeses.contains(fecha.getMonthValue()) && precioConFecha.getSalon().getId() == salon.getId()){
					return new ExtraVariableContainer(extraId, extraNombre, precioConFecha.getPrecio(), extraCantidad);
				}
			}
		}
		return new ExtraVariableContainer(extraId, extraNombre, 0, extraCantidad);
	}

}
