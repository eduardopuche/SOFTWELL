package com.softwell.softwell.controller;

import com.softwell.softwell.model.Pedido;
import com.softwell.softwell.model.Usuario;
import com.softwell.softwell.service.PedidoService;
import com.softwell.softwell.service.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class PedidoController {

    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;

    public PedidoController(PedidoService pedidoService, UsuarioService usuarioService) {
        this.pedidoService = pedidoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/pedidos/{id}")
    public String verDetallePedido(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) return "redirect:/login";

        Usuario usuario = usuarioService.obtenerPorEmail(userDetails.getUsername());
        if (usuario == null) return "redirect:/login";

        Optional<Pedido> pedidoOpt = pedidoService.findById(id);
        // Validación más segura para evitar que se rompa si usuarioId es nulo
        if (pedidoOpt.isEmpty() || pedidoOpt.get().getUsuarioId() == null || !pedidoOpt.get().getUsuarioId().equals(usuario.getId())) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para ver este pedido.");
            return "redirect:/perfil";
        }

        model.addAttribute("usuario", usuario); // Necesario para que el header/layout no falle
        model.addAttribute("pedido", pedidoOpt.get());
        return "pedido-detalle";
    }
}