package com.softwell.softwell.controller;

import com.softwell.softwell.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegistroController {

    private final UsuarioService usuarioService;

    public RegistroController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/registro")
    public String registroPage() {
        return "registro"; 
    }

    @PostMapping("/registro")
    public String procesarRegistro(
            
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) Double latitud,
            @RequestParam(required = false) Double longitud,
            @RequestParam(required = false) String rol,
            RedirectAttributes redirectAttributes,
            Model model) {
        
       
        if (nombre == null || nombre.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            
            model.addAttribute("error", "Nombre, email y contraseña son campos obligatorios.");
            
            model.addAttribute("nombre", nombre);
            model.addAttribute("email", email);
            model.addAttribute("telefono", telefono);
            model.addAttribute("direccion", direccion);
            return "registro";
        }

        try {
            // Pasa el rol deseado directamente al servicio
            usuarioService.registrarUsuario(email, password, nombre, telefono, direccion, latitud, longitud, rol);
            redirectAttributes.addFlashAttribute("success", "¡Te has registrado exitosamente! Ahora puedes iniciar sesión.");
            return "redirect:/login";
        } catch (RuntimeException e) {
          
            model.addAttribute("error", e.getMessage());
            model.addAttribute("nombre", nombre);
            model.addAttribute("email", email);
            model.addAttribute("telefono", telefono);
            model.addAttribute("direccion", direccion);
            return "registro";
        }
    }
}
