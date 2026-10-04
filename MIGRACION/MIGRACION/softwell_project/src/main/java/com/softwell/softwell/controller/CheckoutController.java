package com.softwell.softwell.controller;

import com.softwell.softwell.model.Usuario;
import com.softwell.softwell.model.Pedido;
import com.softwell.softwell.service.PedidoService;
import com.softwell.softwell.service.UsuarioService;
import com.softwell.softwell.model.CartItem;
import com.softwell.softwell.service.CartService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;
    private final CartService cartService;

    public CheckoutController(PedidoService pedidoService, UsuarioService usuarioService, CartService cartService) {
        this.pedidoService = pedidoService;
        this.usuarioService = usuarioService;
        this.cartService = cartService;
    }

    @GetMapping
    public String resumen(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        // Validación: No permitir entrar al resumen si el carrito está vacío
        if (cartService.isEmpty()) {
            return "redirect:/carrito";
        }

        Usuario usuario = usuarioService.obtenerPorEmail(userDetails.getUsername());
        model.addAttribute("usuario", usuario);
        
        List<CartItem> items = cartService.getItems();
        double subtotal = cartService.getTotal();
        double iva = subtotal * 0.19;
        double envio = 15.0;

        model.addAttribute("items", items);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("iva", iva);
        model.addAttribute("envio", envio);
        model.addAttribute("totalFinal", subtotal + iva + envio);
        
        return "checkout";
    }

    @PostMapping("/confirmar")
    public String procesarPago(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam(required = false) String metodoPago,
                               @RequestParam(required = false, defaultValue = "0") Double saldoDisponible,
                               @RequestParam(required = false) Double totalCompra,
                               RedirectAttributes redirectAttributes) {
        
        if (userDetails == null) return "redirect:/login";

        List<CartItem> items = cartService.getItems();
        if (items.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El carrito está vacío.");
            return "redirect:/carrito";
        }

        Usuario usuario = usuarioService.obtenerPorEmail(userDetails.getUsername());
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("error", "Sesión inválida.");
            return "redirect:/login";
        }

        // RECALCULO EN SERVIDOR: Evita que el código se rompa si 'totalCompra' no llega del formulario
        double subtotal = cartService.getTotal();
        double ivaCalculado = subtotal * 0.19;
        double envioCalculado = 15.0;
        double totalCalculado = subtotal + ivaCalculado + envioCalculado;

        // Punto 5: Verificación de dinero suficiente
        if (saldoDisponible < totalCalculado) {
            redirectAttributes.addFlashAttribute("error", "Transacción Rechazada: No cuenta con suficiente dinero en su cuenta bancaria.");
            return "redirect:/checkout";
        }

        // Punto 3 y 4: Simulación de Gateway y 3D Secure
        boolean pagoAutorizado = true; 

        if (pagoAutorizado) {
            Pedido pedido = pedidoService.crearPedido(
                items, usuario.getNombre(), usuario.getEmail(),
                usuario.getTelefono(), usuario.getDireccion(), usuario, totalCalculado, ivaCalculado, envioCalculado
            );

            // Punto 5: Lógica de aprobación -> Restar stock y marcar pagado
            pedidoService.procesarPagoPedido(pedido.getId(), true);

            // Limpiar carrito tras éxito
            cartService.clear();

            // Punto 6: Confirmación y Recibo (Simulación de envío de correo)
            redirectAttributes.addFlashAttribute("success", "¡Compra Exitosa! Pedido #" + pedido.getId() + ". Se ha enviado un recibo electrónico a su correo.");
            return "redirect:/perfil";
        }

        return "redirect:/checkout";
    }
}