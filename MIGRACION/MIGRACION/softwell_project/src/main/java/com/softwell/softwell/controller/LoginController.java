package com.softwell.softwell.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class LoginController {

    /**
     * Muestra la página de login.
     *
     * El parámetro "error" lo agrega Spring Security automáticamente en la URL
     * cuando las credenciales son incorrectas (/login?error=true).
     *
     * El parámetro "logout" lo agrega al redirigir tras cerrar sesión (/login?logout=true).
     */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("error", "Email o contraseña incorrectos, o tu cuenta está desactivada.");
        }
        if (logout != null) {
            model.addAttribute("success", "Has cerrado sesión correctamente.");
        }
        return "login";
    }
}