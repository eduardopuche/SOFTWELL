package com.softwell.softwell.controller;

import com.softwell.softwell.model.Product;
import com.softwell.softwell.model.Proveedor;
import com.softwell.softwell.model.Usuario;
import com.softwell.softwell.service.ProductService;
import com.softwell.softwell.service.ProveedorService;
import com.softwell.softwell.service.UsuarioService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/proveedor")
public class ProveedorController {

    private final ProductService productService;
    private final ProveedorService proveedorService;
    private final UsuarioService usuarioService;

    public ProveedorController(ProductService productService, ProveedorService proveedorService, UsuarioService usuarioService) {
        this.productService = productService;
        this.proveedorService = proveedorService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String dashboard(Model model, Principal principal) {
        String email = principal.getName();
        model.addAttribute("username", email);

        Optional<Proveedor> proveedorOpt = proveedorService.findByEmail(email);

        if (proveedorOpt.isEmpty()) {
            Usuario usuario = usuarioService.obtenerPorEmail(email);
            if (usuario != null && usuario.esProveedor()) {
                model.addAttribute("error", "Tu perfil comercial no está sincronizado. Por favor, actualiza tu información en 'Mi Perfil' para activarlo correctamente.");
            }
        }

        if (proveedorOpt.isPresent()) {
            Proveedor proveedor = proveedorOpt.get();
            List<Product> products = productService.findByProveedorId(proveedor.getId());

            long totalJoyas = products.size();

            int stockEnTienda = products.stream()
                    .filter(p -> "ACEPTADA".equals(p.getEstadoAdmin()))
                    .mapToInt(Product::getStockProveedor)
                    .sum();

            int unidadesPendientes = products.stream()
                    .filter(p -> "PENDIENTE".equals(p.getEstadoAdmin()))
                    .mapToInt(Product::getCantidadOfertada)
                    .sum();

            double inversionPotencial = products.stream()
                    .filter(p -> "ACEPTADA".equals(p.getEstadoAdmin()))
                    .mapToDouble(p -> p.getCostoProveedor() * p.getStockProveedor())
                    .sum();

            model.addAttribute("proveedor", proveedor);
            model.addAttribute("productos", products);
            model.addAttribute("totalJoyas", totalJoyas);
            model.addAttribute("totalStock", stockEnTienda);
            model.addAttribute("unidadesPendientes", unidadesPendientes);
            model.addAttribute("inversionPotencial", inversionPotencial);
        } else {
            model.addAttribute("error", "No se encontró el perfil de proveedor asociado a tu cuenta.");
        }
        return "proveedor-dashboard";
    }

    @GetMapping("/productos")
    public String showProveedorProducts(Model model, Principal principal) {
        Optional<Proveedor> proveedorOpt = proveedorService.findByEmail(principal.getName());
        if (proveedorOpt.isPresent()) {
            Proveedor proveedor = proveedorOpt.get();
            model.addAttribute("proveedor", proveedor);
            model.addAttribute("productos", productService.findByProveedorId(proveedor.getId()));
            return "proveedor-productos";
        } else {
            return "redirect:/proveedor";
        }
    }

    @GetMapping("/productos/nuevo")
    public String showAddProductForm(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        Optional<Proveedor> proveedorOpt = proveedorService.findByEmail(principal.getName());
        if (proveedorOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No se encontró el perfil de proveedor asociado a tu cuenta.");
            return "redirect:/proveedor";
        }
        model.addAttribute("producto", new Product());
        model.addAttribute("proveedor", proveedorOpt.get());
        return "proveedor-producto-form";
    }

    @PostMapping("/productos/guardar")
    public String saveProduct(
            @ModelAttribute("producto") Product product,
            @RequestParam(value = "imagenArchivo", required = false) MultipartFile imagenArchivo,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        Optional<Proveedor> proveedorOpt = proveedorService.findByEmail(principal.getName());
        if (proveedorOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Error: No se pudo asociar el producto a un proveedor válido.");
            return "redirect:/proveedor/productos";
        }

        Proveedor proveedor = proveedorOpt.get();

        // Tratar id vacío como null (formulario HTML envía "" en vez de null)
        if (product.getId() != null && product.getId().isBlank()) {
            product.setId(null);
        }

        if (product.getId() != null) {
            // --- EDICIÓN de producto existente ---
            Optional<Product> existingOpt = productService.findById(product.getId());
            if (existingOpt.isEmpty() || existingOpt.get().getProveedor() == null
                    || !existingOpt.get().getProveedor().getId().equals(proveedor.getId())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para modificar esta joya.");
                return "redirect:/proveedor/productos";
            }

            Product existing = existingOpt.get();

            // El stock de la tienda siempre lo controla el admin, nunca el proveedor
            product.setStock(existing.getStock());

            if ("RECHAZADA".equals(existing.getEstadoAdmin())) {
                // Proveedor editó una oferta rechazada: vuelve a revisión del admin
                product.setEstadoAdmin("PENDIENTE");
                product.setFechaOferta(LocalDateTime.now());
                // La cantidadOfertada viene del formulario, no la pisamos

            } else if ("ACEPTADA".equals(existing.getEstadoAdmin()) && product.getCantidadOfertada() > 0) {
                // Está ofertando más stock: va a revisión del admin
                product.setEstadoAdmin("PENDIENTE");
                product.setFechaOferta(LocalDateTime.now());
                // La cantidadOfertada viene del formulario, no la pisamos

            } else {
                // Estado PENDIENTE: mantiene estado y fecha, pero SÍ permite actualizar cantidadOfertada
                product.setEstadoAdmin(existing.getEstadoAdmin());
                product.setFechaOferta(existing.getFechaOferta());
                // NO pisamos cantidadOfertada: el proveedor puede actualizarla libremente
            }

            if (imagenArchivo == null || imagenArchivo.isEmpty()) {
                product.setImagen(existing.getImagen());
            }

        } else {
            // --- PRODUCTO NUEVO ---
            product.setStock(0);
            product.setEstadoAdmin("PENDIENTE");
            product.setFechaOferta(LocalDateTime.now());
        }

        // Sanitizar nombre de archivo para evitar path traversal
        if (imagenArchivo != null && !imagenArchivo.isEmpty()) {
            try {
                Path rootPath = Paths.get("uploads").toAbsolutePath().normalize();
                if (!Files.exists(rootPath)) {
                    Files.createDirectories(rootPath);
                }
                String originalName = Paths.get(imagenArchivo.getOriginalFilename()).getFileName().toString();
                String fileName = UUID.randomUUID().toString() + "_" + originalName;
                Path filePath = rootPath.resolve(fileName).normalize();

                if (!filePath.startsWith(rootPath)) {
                    redirectAttributes.addFlashAttribute("error", "Nombre de archivo inválido.");
                    return "redirect:/proveedor/productos";
                }

                Files.write(filePath, imagenArchivo.getBytes());
                product.setImagen("/uploads/" + fileName);
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Error al subir la imagen de la joya.");
                return "redirect:/proveedor/productos";
            }
        }

        product.setProveedor(proveedor);
        productService.save(product);

        String msg = (product.getId() == null)
                ? "Joya registrada y ofertada al administrador."
                : "Joya actualizada correctamente.";
        redirectAttributes.addFlashAttribute("success", msg);
        return "redirect:/proveedor/productos";
    }

    @GetMapping("/productos/editar/{id}")
    public String showEditProductForm(@PathVariable String id, Model model, Principal principal, RedirectAttributes redirectAttributes) {
        Optional<Proveedor> proveedorOpt = proveedorService.findByEmail(principal.getName());
        Optional<Product> productOpt = productService.findById(id);

        if (proveedorOpt.isEmpty() || productOpt.isEmpty() || productOpt.get().getProveedor() == null
                || !productOpt.get().getProveedor().getId().equals(proveedorOpt.get().getId())) {
            redirectAttributes.addFlashAttribute("error", "No se encontró la joya o no tienes permiso para acceder.");
            return "redirect:/proveedor/productos";
        }

        model.addAttribute("producto", productOpt.get());
        model.addAttribute("proveedor", proveedorOpt.get());
        return "proveedor-producto-form";
    }

    @GetMapping("/perfil")
    public String verPerfil(Model model, Principal principal) {
        Usuario usuario = usuarioService.obtenerPorEmail(principal.getName());
        if (usuario == null) {
            return "redirect:/login";
        }
        model.addAttribute("usuario", usuario);
        return "perfil";
    }

    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(
            @ModelAttribute Usuario usuarioForm,
            @RequestParam(value = "fotoArchivo", required = false) MultipartFile fotoArchivo,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        Usuario usuarioActual = usuarioService.obtenerPorEmail(principal.getName());
        if (usuarioActual == null) {
            return "redirect:/login";
        }

        String rutaFoto = null;
        if (fotoArchivo != null && !fotoArchivo.isEmpty()) {
            try {
                Path rootPath = Paths.get("uploads").toAbsolutePath().normalize();
                if (!Files.exists(rootPath)) {
                    Files.createDirectories(rootPath);
                }
                String originalName = Paths.get(fotoArchivo.getOriginalFilename()).getFileName().toString();
                String fileName = UUID.randomUUID().toString() + "_" + originalName;
                Path filePath = rootPath.resolve(fileName).normalize();

                if (!filePath.startsWith(rootPath)) {
                    redirectAttributes.addFlashAttribute("error", "Nombre de archivo inválido.");
                    return "redirect:/proveedor/perfil";
                }

                Files.write(filePath, fotoArchivo.getBytes());
                rutaFoto = "/uploads/" + fileName;
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Error al procesar la imagen de perfil.");
                return "redirect:/proveedor/perfil";
            }
        }

        try {
            String emailAnterior = usuarioActual.getEmail();

            usuarioService.actualizarUsuario(
                    usuarioActual.getId(),
                    usuarioForm.getEmail(),
                    usuarioForm.getNombre(),
                    usuarioForm.getTelefono(),
                    usuarioForm.getDireccion(),
                    rutaFoto,
                    usuarioForm.getLatitud(),
                    usuarioForm.getLongitud()
            );

            if (!emailAnterior.equals(usuarioForm.getEmail())) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                Authentication newAuth = new UsernamePasswordAuthenticationToken(
                        usuarioForm.getEmail(), auth.getCredentials(), auth.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(newAuth);
            }

            redirectAttributes.addFlashAttribute("success", "¡Tu información de proveedor ha sido actualizada con éxito!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Hubo unError al actualizar el perfil: " + e.getMessage());
        }

        return "redirect:/proveedor/perfil";
    }

    @GetMapping("/historial-ofertas")
    public String historialOfertas(Model model, Principal principal) {
        Optional<Proveedor> proveedorOpt = proveedorService.findByEmail(principal.getName());
        if (proveedorOpt.isEmpty()) return "redirect:/proveedor";

        Proveedor proveedor = proveedorOpt.get();
        List<Product> todos = productService.findByProveedorId(proveedor.getId());

        List<Product> aceptadas = todos.stream()
            .filter(p -> "ACEPTADA".equals(p.getEstadoAdmin()))
            .sorted((a, b) -> {
                if (a.getFechaOferta() == null && b.getFechaOferta() == null) return 0;
                if (a.getFechaOferta() == null) return 1;
                if (b.getFechaOferta() == null) return -1;
                return b.getFechaOferta().compareTo(a.getFechaOferta());
            })
            .collect(java.util.stream.Collectors.toList());

        List<Product> rechazadas = todos.stream()
            .filter(p -> "RECHAZADA".equals(p.getEstadoAdmin()))
            .sorted((a, b) -> {
                if (a.getFechaOferta() == null && b.getFechaOferta() == null) return 0;
                if (a.getFechaOferta() == null) return 1;
                if (b.getFechaOferta() == null) return -1;
                return b.getFechaOferta().compareTo(a.getFechaOferta());
            })
            .collect(java.util.stream.Collectors.toList());

        model.addAttribute("proveedor", proveedor);
        model.addAttribute("ofertasAceptadas", aceptadas);
        model.addAttribute("ofertasRechazadas", rechazadas);
        return "proveedor-historial-ofertas";
    }

}