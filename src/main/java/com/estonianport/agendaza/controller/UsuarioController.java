package com.estonianport.agendaza.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.estonianport.agendaza.commons.data.GeneralPath;
import com.estonianport.agendaza.model.Rol;
import com.estonianport.agendaza.model.Salon;
import com.estonianport.agendaza.model.Usuario;
import com.estonianport.agendaza.service.RolService;
import com.estonianport.agendaza.service.UsuarioService;

import jakarta.servlet.http.HttpSession;


@Controller
public class UsuarioController {

	@Autowired
	private UsuarioService usuarioService;

	@Autowired
	private RolService rolService;

	@RequestMapping("/abmUsuario")
	public String abm(Model model, HttpSession session) {
		model.addAttribute("listaUsuario", usuarioService.getAll());
		
		// Salon en sesion para volver al calendario
		Salon salon = (Salon) session.getAttribute(GeneralPath.SALON);
		model.addAttribute(GeneralPath.SALON, salon);

		return GeneralPath.USUARIO + GeneralPath.PATH_SEPARATOR + GeneralPath.ABM_USUARIO;
	}

	@GetMapping("/saveUsuario/{id}")
	public String showSave(@PathVariable("id") Long id, Model model) {
		List<Rol> listaRoles = rolService.getAll();
		model.addAttribute("listaRoles", listaRoles);

		if(id != null && id != 0) {
			model.addAttribute(GeneralPath.USUARIO, usuarioService.get(id));
		}else {
			model.addAttribute(GeneralPath.USUARIO, new Usuario());
		}
		return GeneralPath.USUARIO + GeneralPath.PATH_SEPARATOR + GeneralPath.SAVE_USUARIO;
	}

	@PostMapping("/saveUsuario")
	public String save(Usuario usuario, Model model) {
		usuarioService.save(usuario);
		return GeneralPath.REDIRECT + GeneralPath.ABM_USUARIO;
	}

	@GetMapping("/deleteUsuario/{id}")
	public String delete(@PathVariable("id") Long id, Model model) {
		usuarioService.delete(id);
		return GeneralPath.REDIRECT +  GeneralPath.ABM_USUARIO;
	}
}
