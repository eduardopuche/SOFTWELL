package com.softwell.softwell.controller;

import com.softwell.softwell.model.Pedido;
import com.softwell.softwell.model.Usuario;
import com.softwell.softwell.model.Testimonio;
import com.softwell.softwell.service.PedidoService;
import com.softwell.softwell.service.UsuarioService;
import com.softwell.softwell.service.TestimonioService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

/**
 * PerfilController actualizado para usar Spring Security.
 * Se reemplaza HttpSession por @AuthenticationPrincipal UserDetails.
 * La protección de acceso la maneja SecurityConfig (/perfil/** requiere login).
 */
@Controller
public class PerfilController {

    private final UsuarioService usuarioService;
    private final PedidoService pedidoService;
    private final TestimonioService testimonioService;

    public PerfilController(UsuarioService usuarioService, PedidoService pedidoService, TestimonioService testimonioService) {
        this.usuarioService = usuarioService;
        this.pedidoService = pedidoService;
        this.testimonioService = testimonioService;
    }

    @GetMapping("/perfil")
    public String verPerfil(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        Usuario usuario = usuarioService.obtenerPorEmail(userDetails.getUsername());
        model.addAttribute("usuario", usuario);

        if (usuario != null) {
            List<Pedido> pedidos = pedidoService.findByUsuarioId(usuario.getId());
            model.addAttribute("pedidos", pedidos);
        }

        return "perfil";
    }

    @GetMapping("/perfil/pedidos/{id}") // Cambiado para evitar ambigüedad
    public String verDetallePedido(@PathVariable String id, @AuthenticationPrincipal UserDetails userDetails, Model model, RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        Usuario usuario = usuarioService.obtenerPorEmail(userDetails.getUsername());
        if (usuario == null) {
            return "redirect:/login";
        }

        Optional<Pedido> pedidoOpt = pedidoService.findById(id);

        // Validación usando emailCliente, ya que getUsuario() no está definido en el modelo Pedido
        if (pedidoOpt.isEmpty() || pedidoOpt.get().getEmailCliente() == null || 
            !pedidoOpt.get().getEmailCliente().equals(usuario.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Pedido no encontrado o no tienes permiso para verlo.");
            return "redirect:/perfil";
        }

        model.addAttribute("usuario", usuario); // Se agrega para que el layout/header funcione correctamente
        model.addAttribute("pedido", pedidoOpt.get());
        return "pedido-detalle";
    }

    @PostMapping("/perfil")
    public String actualizarPerfil(
            @ModelAttribute("usuario") Usuario usuarioForm,
            @RequestParam(value = "fotoArchivo", required = false) MultipartFile fotoArchivo,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        Usuario usuarioActual = usuarioService.obtenerPorEmail(userDetails.getUsername());

        if (usuarioActual == null) {
            return "redirect:/login";
        }

        String rutaFoto = null;
        if (fotoArchivo != null && !fotoArchivo.isEmpty()) {
            try {
                // Aseguramos que el directorio exista en la raíz del proyecto
                Path rootPath = Paths.get("uploads").toAbsolutePath().normalize();
                if (!Files.exists(rootPath)) {
                    Files.createDirectories(rootPath);
                }
                
                String fileName = UUID.randomUUID().toString() + "_" + fotoArchivo.getOriginalFilename();
                Path filePath = rootPath.resolve(fileName).normalize();
                Files.write(filePath, fotoArchivo.getBytes());
                rutaFoto = "/uploads/" + fileName; // Esta es la ruta web que se guardará
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Error al subir la imagen.");
                return "redirect:/perfil";
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

            // Si el email cambió, actualizamos el SecurityContext para evitar error 403
            if (!emailAnterior.equals(usuarioForm.getEmail())) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                Authentication newAuth = new UsernamePasswordAuthenticationToken(usuarioForm.getEmail(), auth.getCredentials(), auth.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(newAuth);
            }

            redirectAttributes.addFlashAttribute("success", "¡Tu perfil ha sido actualizado exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Hubo un error al actualizar tu perfil.");
        }

        return "redirect:/perfil";
    }

    @PostMapping("/perfil/nuevo-testimonio")
    public String guardarTestimonio(
            String texto,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (texto == null || texto.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El texto del testimonio no puede estar vacío.");
            return "redirect:/perfil";
        }

        Usuario usuario = usuarioService.obtenerPorEmail(userDetails.getUsername());

        Testimonio nuevoTestimonio = new Testimonio();
        nuevoTestimonio.setNombreCliente(usuario.getNombre());
        nuevoTestimonio.setTexto(texto);
        nuevoTestimonio.setImagenCliente(usuario.getFotoPerfil());
        nuevoTestimonio.setVisible(true);
        testimonioService.save(nuevoTestimonio);

        redirectAttributes.addFlashAttribute("success", "¡Gracias! Tu testimonio ha sido publicado exitosamente.");
        return "redirect:/perfil";
    }
}