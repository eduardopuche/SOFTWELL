package com.softwell.softwell.controller;

import com.softwell.softwell.model.Product;
import com.softwell.softwell.service.CartService;
import com.softwell.softwell.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/productos")
public class ProductController {

    private final ProductService productService;
    private final CartService cartService;

    public ProductController(ProductService productService, CartService cartService) {
        this.productService = productService;
        this.cartService = cartService;
    }

    @GetMapping({"", "/"})
    public String redirectToCatalog() {
        return "redirect:/catalogo";
    }

    @GetMapping("/{id}")
    public String showProductDetail(@PathVariable String id, Model model) {
        Optional<Product> product = productService.findById(id);
        if (product.isEmpty()) {
            return "redirect:/catalogo";
        }
        model.addAttribute("producto", product.get());
        model.addAttribute("cartItemCount", cartService.getTotalItems());
        return "producto-detalle";
    }

    @PostMapping("/agregar-carrito")
    public String addToCart(
            @RequestParam String productId,
            @RequestParam(defaultValue = "1") int cantidad,
            RedirectAttributes redirectAttributes) {
        
        Optional<Product> product = productService.findById(productId);
        if (product.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
            return "redirect:/catalogo";
        }

        if (product.get().getStock() < cantidad) {
            redirectAttributes.addFlashAttribute("error", "Stock insuficiente");
            return "redirect:/productos/" + productId;
        }

        cartService.addItem(product.get(), cantidad);
        redirectAttributes.addFlashAttribute("success", "Producto agregado al carrito");
        return "redirect:/productos/" + productId;
    }
}