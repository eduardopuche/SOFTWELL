package com.softwell.softwell.controller; // Corregido el nombre del paquete

import com.softwell.softwell.service.CartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/carrito")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String showCart(Model model) {
        model.addAttribute("items", cartService.getItems());
        model.addAttribute("total", cartService.getTotal());
        model.addAttribute("cartItemCount", cartService.getTotalItems());
        return "carrito";
    }

    @PostMapping("/eliminar/{productId}")
    public String removeFromCart(@PathVariable String productId, RedirectAttributes redirectAttributes) {
        cartService.removeItem(productId);
        redirectAttributes.addFlashAttribute("success", "Producto eliminado del carrito");
        return "redirect:/carrito";
    }

    @PostMapping("/actualizar/{productId}")
    public String updateQuantity(
            @PathVariable String productId,
            @RequestParam int cantidad,
            RedirectAttributes redirectAttributes) {

        if (cantidad <= 0) {
            cartService.removeItem(productId);
        } else {
            cartService.updateQuantity(productId, cantidad);
        }
        redirectAttributes.addFlashAttribute("success", "Cantidad actualizada");
        return "redirect:/carrito";
    }

    // Funcionalidad de "Seguir con el pago" desde el carrito
    @GetMapping("/checkout")
    public String proceedToCheckout() {
        return "redirect:/checkout";
    }

}