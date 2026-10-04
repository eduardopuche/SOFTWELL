package com.softwell.softwell.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.softwell.softwell.service.CartService;
import com.softwell.softwell.service.ProductService;
import com.softwell.softwell.service.TestimonioService;

import jakarta.servlet.http.HttpSession;
@Controller
public class HomeController {

    private final ProductService productService;
    private final CartService cartService;
    private final TestimonioService testimonioService;

    public HomeController(ProductService productService, CartService cartService, TestimonioService testimonioService) {
        this.productService = productService;
        this.cartService = cartService;
        this.testimonioService = testimonioService;
    }

    @GetMapping("/")
    public String showHomePage(Model model) {
        model.addAttribute("destacados", productService.findDestacados(6)); 
        model.addAttribute("temporada", productService.findNuevos(4));
        model.addAttribute("testimonios", testimonioService.findVisibles());
        model.addAttribute("cartItemCount", cartService.getTotalItems());
        return "index";
    }

    @GetMapping("/catalogo")
    public String showCatalogo(@RequestParam(value = "categoria", required = false, defaultValue = "all") String categoria, Model model) {
        model.addAttribute("productos", productService.findByCategoria(categoria));
        model.addAttribute("categoriaSeleccionada", categoria);
        model.addAttribute("cartItemCount", cartService.getTotalItems());
        return "catalogo";
    }
}