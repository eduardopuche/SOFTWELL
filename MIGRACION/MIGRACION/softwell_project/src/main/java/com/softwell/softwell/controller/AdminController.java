package com.softwell.softwell.controller;

import com.softwell.softwell.model.Product;
import com.softwell.softwell.service.PedidoService;
import com.softwell.softwell.model.Usuario;
import com.softwell.softwell.model.Testimonio;
import com.softwell.softwell.model.Proveedor;
import com.softwell.softwell.service.TestimonioService;
import com.softwell.softwell.service.UsuarioService;
import com.softwell.softwell.service.ProveedorService;
import com.softwell.softwell.service.ProductService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
import java.util.UUID;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.LinkedHashMap;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProductService productService;
    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;
    private final TestimonioService testimonioService;
    private final ProveedorService proveedorService;

    public AdminController(ProductService productService, PedidoService pedidoService, UsuarioService usuarioService, TestimonioService testimonioService, ProveedorService proveedorService) {
        this.productService = productService;
        this.pedidoService = pedidoService;
        this.usuarioService = usuarioService;
        this.testimonioService = testimonioService;
        this.proveedorService = proveedorService;
    }

    private String sanitizarNombreArchivo(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) return "archivo";
        return Paths.get(originalFilename).getFileName().toString();
    }

    private String guardarArchivo(MultipartFile archivo, RedirectAttributes redirectAttributes) throws IOException {
        Path rootPath = Paths.get("uploads").toAbsolutePath().normalize();
        if (!Files.exists(rootPath)) {
            Files.createDirectories(rootPath);
        }
        String originalName = sanitizarNombreArchivo(archivo.getOriginalFilename());
        String fileName = UUID.randomUUID().toString() + "_" + originalName;
        Path filePath = rootPath.resolve(fileName).normalize();

        if (!filePath.startsWith(rootPath)) {
            throw new IOException("Nombre de archivo inválido (path traversal detectado).");
        }

        Files.write(filePath, archivo.getBytes());
        return "/uploads/" + fileName;
    }

    @GetMapping
    public String dashboard(Model model) {
        List<Product> allProducts = productService.findAll();

        List<Product> inventory = allProducts.stream()
                .filter(p -> "ACEPTADA".equals(p.getEstadoAdmin()))
                .collect(Collectors.toList());

        List<Product> offers = allProducts.stream()
                .filter(p -> "PENDIENTE".equals(p.getEstadoAdmin()))
                .collect(Collectors.toList());

        model.addAttribute("totalProductos",
                inventory.stream().mapToInt(Product::getStock).sum());
        model.addAttribute("totalOfertas", offers.size());
        model.addAttribute("totalJoyasOfertadas",
                offers.stream().mapToInt(Product::getCantidadOfertada).sum());
        model.addAttribute("totalDestacados",
                inventory.stream().filter(Product::isDestacado).count());
        model.addAttribute("totalProveedores",
                proveedorService.findAll().size());
        model.addAttribute("totalClientes",
                usuarioService.contarClientes());
        model.addAttribute("totalPedidos",
                pedidoService.findAll().size());
        model.addAttribute("productosDestacados",
                inventory.stream().filter(Product::isDestacado).collect(Collectors.toList()));

        // Alertas de stock
        List<Product> agotados = inventory.stream()
                .filter(p -> p.getStock() == 0)
                .collect(Collectors.toList());
        List<Product> bajoStock = inventory.stream()
                .filter(p -> p.getStock() > 0 && p.getStock() <= 2)
                .collect(Collectors.toList());
        model.addAttribute("productosAgotados", agotados);
        model.addAttribute("productosBajoStock", bajoStock);

        return "admin-dashboard";
    }

    @GetMapping("/productos")
    public String showProductsPage(Model model) {
        List<Product> inventory = productService.findAll().stream()
                .filter(p -> "ACEPTADA".equals(p.getEstadoAdmin()))
                .collect(Collectors.toList());
        model.addAttribute("productos", inventory);
        return "admin-products";
    }

    @GetMapping("/ofertas")
    public String showOffersPage(Model model) {
        List<Product> ofertas = productService.findAll().stream()
                .filter(p -> "PENDIENTE".equals(p.getEstadoAdmin()))
                .collect(Collectors.toList());

        model.addAttribute("ofertas", ofertas);
        model.addAttribute("cantidadOfertas", ofertas.size());
        model.addAttribute("unidadesOfertadas",
                ofertas.stream().mapToInt(Product::getCantidadOfertada).sum());

        return "admin-ofertas";
    }

    @PostMapping("/ofertas/aceptar/{id}")
    public String aceptarOferta(
            @PathVariable String id,
            @RequestParam(value = "unidadesAceptadas", required = false) Integer unidadesAceptadas,
            @RequestParam(value = "unidadesEnTienda", required = false) Integer unidadesEnTienda,
            @RequestParam(value = "precioVenta", required = false) Double precioVenta,
            RedirectAttributes redirectAttributes) {

        Optional<Product> offerOpt = productService.findById(id);

        if (offerOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No se encontró la oferta.");
            return "redirect:/admin/ofertas";
        }

        Product offer = offerOpt.get();

        // --- Validaciones del servidor (por si alguien evita el JS del modal) ---
        if (unidadesAceptadas == null || unidadesAceptadas < 1) {
            redirectAttributes.addFlashAttribute("error", "Las unidades a aceptar deben ser al menos 1.");
            return "redirect:/admin/ofertas";
        }

        if (unidadesAceptadas > offer.getCantidadOfertada()) {
            redirectAttributes.addFlashAttribute("error",
                "Las unidades a aceptar (" + unidadesAceptadas + ") no pueden superar la cantidad ofertada (" + offer.getCantidadOfertada() + ").");
            return "redirect:/admin/ofertas";
        }

        if (unidadesEnTienda == null || unidadesEnTienda < 1) {
            redirectAttributes.addFlashAttribute("error", "Las unidades a mostrar en tienda deben ser al menos 1.");
            return "redirect:/admin/ofertas";
        }

        if (unidadesEnTienda > unidadesAceptadas) {
            redirectAttributes.addFlashAttribute("error",
                "Las unidades en tienda (" + unidadesEnTienda + ") no pueden superar las unidades aceptadas (" + unidadesAceptadas + ").");
            return "redirect:/admin/ofertas";
        }

        if (precioVenta == null || precioVenta <= 0) {
            redirectAttributes.addFlashAttribute("error", "El precio de venta debe ser mayor a 0.");
            return "redirect:/admin/ofertas";
        }

        // --- Lógica de aceptación ---
        // Cada oferta aceptada crea su propio producto independiente
        // (el mismo producto puede tener precios distintos en diferentes momentos)
        int unidadesSobrantes = offer.getCantidadOfertada() - unidadesAceptadas;

        offer.setEstadoAdmin("ACEPTADA");
        offer.setStock(unidadesEnTienda);
        offer.setStockProveedor(unidadesAceptadas);
        offer.setCantidadOfertada(0);
        offer.setPrecio(precioVenta);
        productService.save(offer);
        redirectAttributes.addFlashAttribute("success",
            "Oferta aceptada. '" + offer.getNombre() + "' agregado al inventario con " + unidadesEnTienda + " uds. visibles y precio $" + String.format("%.2f", precioVenta) + ".");

        // Si el admin no aceptó todas las unidades, crear una oferta RECHAZADA
        // con las unidades sobrantes para que el proveedor las vea
        if (unidadesSobrantes > 0) {
            Product devolucion = new Product();
            devolucion.setNombre(offer.getNombre());
            devolucion.setCategoria(offer.getCategoria());
            devolucion.setPrecio(offer.getPrecio());
            devolucion.setCostoProveedor(offer.getCostoProveedor());
            devolucion.setImagen(offer.getImagen());
            devolucion.setDescripcion(offer.getDescripcion());
            devolucion.setDestacado(false);
            devolucion.setProveedor(offer.getProveedor());
            devolucion.setCantidadOfertada(unidadesSobrantes);
            devolucion.setEstadoAdmin("RECHAZADA");
            devolucion.setDetallesVentaAdmin(unidadesAceptadas + " unidades tomadas");
            devolucion.setStock(0);
            devolucion.setStockProveedor(0);
            productService.save(devolucion);
        }

        return "redirect:/admin/ofertas";
    }

    @PostMapping("/ofertas/rechazar/{id}")
    public String rechazarOferta(@PathVariable String id, RedirectAttributes redirectAttributes) {
        Optional<Product> productOpt = productService.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setEstadoAdmin("RECHAZADA");
            // No reseteamos cantidadOfertada para que el proveedor vea cuánto había ofertado y pueda editarlo
            productService.save(product);
            redirectAttributes.addFlashAttribute("success", "La oferta ha sido rechazada.");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se encontró el producto.");
        }
        return "redirect:/admin/ofertas";
    }

    @GetMapping("/productos/nuevo")
    public String showAddProductForm(Model model) {
        model.addAttribute("producto", new Product());
        model.addAttribute("proveedores", proveedorService.findAll());
        return "admin-producto-form";
    }

    @PostMapping("/productos/guardar")
    public String saveProduct(
            @ModelAttribute Product producto,
            @RequestParam(value = "imagenArchivo", required = false) MultipartFile imagenArchivo,
            RedirectAttributes redirectAttributes) {

        // Limpiar proveedor vacío (el select envía objeto vacío en vez de null cuando no se elige)
        if (producto.getProveedor() != null && producto.getProveedor().getId() == null) {
            producto.setProveedor(null);
        }

        // Tratar id vacío como null (formulario HTML envía "" en vez de null)
        if (producto.getId() != null && producto.getId().isBlank()) {
            producto.setId(null);
        }

        if (producto.getId() != null) {
            Optional<Product> existing = productService.findById(producto.getId());
            if (existing.isPresent()) {
                Product oldProduct = existing.get();
                // Siempre preservar stockProveedor y estadoAdmin (nunca vienen del formulario)
                producto.setStockProveedor(oldProduct.getStockProveedor());
                producto.setEstadoAdmin(oldProduct.getEstadoAdmin());
                if (oldProduct.getProveedor() != null) {
                    // Productos de proveedor: preservar stock, precio y demás campos bloqueados
                    producto.setStock(oldProduct.getStock());
                    producto.setPrecio(oldProduct.getPrecio());
                    producto.setProveedor(oldProduct.getProveedor());
                    producto.setCostoProveedor(oldProduct.getCostoProveedor());
                    producto.setCantidadOfertada(oldProduct.getCantidadOfertada());
                }
            }
        }

        try {
            if (imagenArchivo != null && !imagenArchivo.isEmpty()) {
                producto.setImagen(guardarArchivo(imagenArchivo, redirectAttributes));
            } else if (producto.getId() != null) {
                productService.findById(producto.getId()).ifPresent(p -> producto.setImagen(p.getImagen()));
            } else {
                producto.setImagen(null);
            }

            if (producto.getId() == null) {
                producto.setEstadoAdmin("ACEPTADA");
                // Producto nuevo: stockProveedor = stock ingresado (con o sin proveedor)
                producto.setStockProveedor(producto.getStock());
            }
            productService.save(producto);
            redirectAttributes.addFlashAttribute("success", "Producto guardado exitosamente.");
            return "redirect:/admin/productos";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar la imagen: " + e.getMessage());
            return producto.getId() == null
                    ? "redirect:/admin/productos/nuevo"
                    : "redirect:/admin/productos/editar/" + producto.getId();
        }
    }

    @PostMapping("/productos/eliminar-foto/{id}")
    public String eliminarFotoProducto(@PathVariable String id, RedirectAttributes redirectAttributes) {
        Optional<Product> productOpt = productService.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            if (product.getImagen() != null) {
                try {
                    String fotoPath = product.getImagen();
                    if (fotoPath.startsWith("/uploads/")) {
                        String fileName = fotoPath.substring("/uploads/".length());
                        Path filePath = Paths.get("uploads").toAbsolutePath().normalize().resolve(fileName);
                        Files.deleteIfExists(filePath);
                    }
                    product.setImagen(null);
                    productService.save(product);
                    redirectAttributes.addFlashAttribute("success", "Foto eliminada correctamente.");
                } catch (IOException e) {
                    redirectAttributes.addFlashAttribute("error", "Error al eliminar el archivo físico.");
                }
            }
            return "redirect:/admin/productos/editar/" + id;
        }
        return "redirect:/admin/productos";
    }


    @PostMapping("/productos/mover-a-tienda/{id}")
    public String moverBodegaATienda(
            @PathVariable String id,
            @RequestParam int cantidad,
            RedirectAttributes redirectAttributes) {

        Optional<Product> productOpt = productService.findById(id);
        if (productOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Producto no encontrado.");
            return "redirect:/admin/productos";
        }

        Product product = productOpt.get();
        int bodega = product.getStockProveedor() - product.getStock();

        if (cantidad < 1) {
            redirectAttributes.addFlashAttribute("error", "La cantidad debe ser al menos 1.");
            return "redirect:/admin/productos";
        }
        if (cantidad > bodega) {
            redirectAttributes.addFlashAttribute("error",
                "No puedes subir más de lo que hay en bodega (" + bodega + " uds.).");
            return "redirect:/admin/productos";
        }

        product.setStock(product.getStock() + cantidad);
        productService.save(product);
        redirectAttributes.addFlashAttribute("success",
            cantidad + " unidad(es) de '" + product.getNombre() + "' movidas a tienda exitosamente.");
        return "redirect:/admin/productos";
    }

    @GetMapping("/productos/editar/{id}")
    public String showEditProductForm(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Product> producto = productService.findById(id);
        if (producto.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Producto no encontrado.");
            return "redirect:/admin/productos";
        }
        model.addAttribute("producto", producto.get());
        model.addAttribute("proveedores", proveedorService.findAll());
        return "admin-producto-form";
    }

    @PostMapping("/productos/eliminar/{id}")
    public String deleteProduct(@PathVariable String id, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "La eliminación de productos está deshabilitada por motivos de integridad del sistema.");
        return "redirect:/admin/productos";
    }

    @PostMapping("/productos/limpiar-todo")
    public String limpiarTodoElStock(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "La función de limpieza total del inventario está bloqueada.");
        return "redirect:/admin/productos";
    }


    @GetMapping("/reportes")
    public String showReportes(Model model) {
        List<com.softwell.softwell.model.Pedido> todosPedidos = pedidoService.findAll();
        List<Product> inventory = productService.findAll().stream()
                .filter(p -> "ACEPTADA".equals(p.getEstadoAdmin()))
                .collect(Collectors.toList());

        // Totales por estado
        long pagados = todosPedidos.stream().filter(p -> "PAGADO".equals(p.getEstado())).count();
        long pendientes = todosPedidos.stream().filter(p -> "PENDIENTE".equals(p.getEstado())).count();
        long rechazados = todosPedidos.stream().filter(p -> "RECHAZADO".equals(p.getEstado())).count();
        double ingresosTotales = todosPedidos.stream()
                .filter(p -> "PAGADO".equals(p.getEstado()))
                .mapToDouble(com.softwell.softwell.model.Pedido::getTotal).sum();

        // Ingresos por mes
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.forLanguageTag("es"));
        Map<String, double[]> porMesMap = new LinkedHashMap<>();
        todosPedidos.stream()
                .filter(p -> "PAGADO".equals(p.getEstado()) && p.getFechaCreacion() != null)
                .sorted((a, b) -> b.getFechaCreacion().compareTo(a.getFechaCreacion()))
                .forEach(p -> {
                    String mes = p.getFechaCreacion().format(fmt);
                    porMesMap.computeIfAbsent(mes, k -> new double[]{0, 0});
                    porMesMap.get(mes)[0]++;
                    porMesMap.get(mes)[1] += p.getTotal();
                });

        List<Map<String, Object>> ingresosPorMes = porMesMap.entrySet().stream().map(e -> {
            Map<String, Object> row = new java.util.HashMap<>();
            row.put("mes", e.getKey());
            row.put("cantidad", (int) e.getValue()[0]);
            row.put("total", e.getValue()[1]);
            return row;
        }).collect(Collectors.toList());

        // Alertas de stock
        List<Product> agotados = inventory.stream().filter(p -> p.getStock() == 0).collect(Collectors.toList());
        List<Product> bajoStock = inventory.stream().filter(p -> p.getStock() > 0 && p.getStock() <= 2).collect(Collectors.toList());

        // Últimos 10 pedidos
        List<com.softwell.softwell.model.Pedido> ultimosPedidos = todosPedidos.stream()
                .filter(p -> p.getFechaCreacion() != null)
                .sorted((a, b) -> b.getFechaCreacion().compareTo(a.getFechaCreacion()))
                .limit(10)
                .collect(Collectors.toList());

        model.addAttribute("ingresosTotales", ingresosTotales);
        model.addAttribute("pedidosPagados", pagados);
        model.addAttribute("pedidosPendientes", pendientes);
        model.addAttribute("pedidosRechazados", rechazados);
        model.addAttribute("ingresosPorMes", ingresosPorMes);
        model.addAttribute("productosAgotados", agotados);
        model.addAttribute("productosBajoStock", bajoStock);
        model.addAttribute("ultimosPedidos", ultimosPedidos);

        return "admin-reportes";
    }

    @GetMapping("/pedidos")
    public String showPedidosPage(Model model) {
        model.addAttribute("pedidos", pedidoService.findAll());
        return "admin-pedidos";
    }

    @GetMapping("/pedidos/{id}")
    public String showPedidoDetalle(@PathVariable String id, @AuthenticationPrincipal UserDetails userDetails, Model model, RedirectAttributes redirectAttributes) {
        Optional<com.softwell.softwell.model.Pedido> pedido = pedidoService.findById(id);
        if (pedido.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Pedido no encontrado.");
            return "redirect:/admin/pedidos";
        }
        Usuario admin = usuarioService.obtenerPorEmail(userDetails.getUsername());
        model.addAttribute("usuario", admin);
        model.addAttribute("pedido", pedido.get());
        return "admin-pedido-detalle";
    }

    @GetMapping("/clientes")
    public String showClientesPage(Model model) {
        model.addAttribute("clientes", usuarioService.obtenerTodosLosClientes());
        return "admin-clientes";
    }

    @GetMapping("/clientes/editar/{id}")
    public String showEditClienteForm(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        Usuario cliente = usuarioService.obtenerPorId(id);
        if (cliente == null || !cliente.esCliente()) {
            redirectAttributes.addFlashAttribute("error", "Cliente no encontrado.");
            return "redirect:/admin/clientes";
        }
        model.addAttribute("cliente", cliente);
        return "admin-cliente-form";
    }

    @PostMapping("/clientes/guardar")
    public String saveCliente(
            @ModelAttribute Usuario cliente,
            @RequestParam(value = "fotoArchivo", required = false) MultipartFile fotoArchivo,
            RedirectAttributes redirectAttributes) {

        String rutaFoto = null;
        if (fotoArchivo != null && !fotoArchivo.isEmpty()) {
            try {
                rutaFoto = guardarArchivo(fotoArchivo, redirectAttributes);
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Error al subir la foto del cliente: " + e.getMessage());
                return "redirect:/admin/clientes";
            }
        }

        usuarioService.actualizarUsuario(
                cliente.getId(),
                cliente.getEmail(),
                cliente.getNombre(),
                cliente.getTelefono(),
                cliente.getDireccion(),
                rutaFoto,
                cliente.getLatitud(),
                cliente.getLongitud()
        );
        redirectAttributes.addFlashAttribute("success", "Cliente actualizado exitosamente.");
        return "redirect:/admin/clientes";
    }

    @PostMapping("/clientes/eliminar-foto/{id}")
    public String eliminarFotoCliente(@PathVariable String id, RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioService.obtenerPorId(id);
        if (usuario != null && usuario.getFotoPerfil() != null) {
            try {
                String fotoPath = usuario.getFotoPerfil();
                if (fotoPath.startsWith("/uploads/")) {
                    String fileName = fotoPath.substring("/uploads/".length());
                    Path filePath = Paths.get("uploads").toAbsolutePath().normalize().resolve(fileName);
                    Files.deleteIfExists(filePath);
                }
                usuario.setFotoPerfil(null);
                usuarioService.save(usuario);
                redirectAttributes.addFlashAttribute("success", "Foto del cliente eliminada.");
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Error al eliminar el archivo físico.");
            }
            return "redirect:/admin/clientes/editar/" + id;
        }
        return "redirect:/admin/clientes";
    }

    @PostMapping("/clientes/activar/{id}")
    public String activarCliente(@PathVariable String id, RedirectAttributes redirectAttributes) {
        usuarioService.activarUsuario(id);
        redirectAttributes.addFlashAttribute("success", "Cliente activado.");
        return "redirect:/admin/clientes";
    }

    @PostMapping("/clientes/desactivar/{id}")
    public String desactivarCliente(@PathVariable String id, RedirectAttributes redirectAttributes) {
        usuarioService.desactivarUsuario(id);
        redirectAttributes.addFlashAttribute("success", "Cliente desactivado.");
        return "redirect:/admin/clientes";
    }

    @GetMapping("/testimonios")
    public String showTestimoniosPage(Model model) {
        model.addAttribute("testimonios", testimonioService.findAll());
        return "admin-testimonios";
    }

    @GetMapping("/testimonios/nuevo")
    public String showAddTestimonioForm(Model model) {
        model.addAttribute("testimonio", new Testimonio());
        return "admin-testimonio-form";
    }

    @GetMapping("/testimonios/editar/{id}")
    public String showEditTestimonioForm(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Testimonio> testimonio = testimonioService.findById(id);
        if (testimonio.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Testimonio no encontrado.");
            return "redirect:/admin/testimonios";
        }
        model.addAttribute("testimonio", testimonio.get());
        return "admin-testimonio-form";
    }

    @PostMapping("/testimonios/guardar")
    public String saveTestimonio(
            @ModelAttribute Testimonio testimonio,
            @RequestParam(value = "fotoArchivo", required = false) MultipartFile fotoArchivo,
            RedirectAttributes redirectAttributes) {

        if (testimonio.getId() != null) {
            Optional<Testimonio> existing = testimonioService.findById(testimonio.getId());
            if (existing.isPresent() && (fotoArchivo == null || fotoArchivo.isEmpty())) {
                testimonio.setImagenCliente(existing.get().getImagenCliente());
            }
        }

        if (fotoArchivo != null && !fotoArchivo.isEmpty()) {
            try {
                testimonio.setImagenCliente(guardarArchivo(fotoArchivo, redirectAttributes));
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Error al subir la foto del testimonio: " + e.getMessage());
                return "redirect:/admin/testimonios";
            }
        }

        testimonio.setVisible(true);
        testimonioService.save(testimonio);
        redirectAttributes.addFlashAttribute("success", "Testimonio guardado y publicado exitosamente.");
        return "redirect:/admin/testimonios";
    }

    @PostMapping("/testimonios/eliminar-foto/{id}")
    public String eliminarFotoTestimonio(@PathVariable String id, RedirectAttributes redirectAttributes) {
        Optional<Testimonio> testOpt = testimonioService.findById(id);
        if (testOpt.isPresent() && testOpt.get().getImagenCliente() != null) {
            try {
                String fotoPath = testOpt.get().getImagenCliente();
                if (fotoPath.startsWith("/uploads/")) {
                    String fileName = fotoPath.substring("/uploads/".length());
                    Path filePath = Paths.get("uploads").toAbsolutePath().normalize().resolve(fileName);
                    Files.deleteIfExists(filePath);
                }
                Testimonio test = testOpt.get();
                test.setImagenCliente(null);
                testimonioService.save(test);
                redirectAttributes.addFlashAttribute("success", "Foto del testimonio eliminada.");
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Error al eliminar el archivo físico.");
            }
            return "redirect:/admin/testimonios/editar/" + id;
        }
        return "redirect:/admin/testimonios";
    }

    @PostMapping("/testimonios/eliminar/{id}")
    public String deleteTestimonio(@PathVariable String id, RedirectAttributes redirectAttributes) {
        testimonioService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Testimonio eliminado exitosamente.");
        return "redirect:/admin/testimonios";
    }

    @GetMapping("/proveedores")
    public String showProveedoresPage(Model model) {
        model.addAttribute("proveedores", proveedorService.findAll());
        return "admin-proveedores";
    }

    @GetMapping("/proveedores/nuevo")
    public String showAddProveedorForm(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        return "admin-proveedor-form";
    }

    @PostMapping("/proveedores/guardar")
    public String saveProveedor(@ModelAttribute Proveedor proveedor, RedirectAttributes redirectAttributes) {
        proveedorService.save(proveedor);
        redirectAttributes.addFlashAttribute("success", "Proveedor guardado exitosamente.");
        return "redirect:/admin/proveedores";
    }

    @GetMapping("/proveedores/editar/{id}")
    public String showEditProveedorForm(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Proveedor> proveedor = proveedorService.findById(id);
        if (proveedor.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Proveedor no encontrado.");
            return "redirect:/admin/proveedores";
        }
        model.addAttribute("proveedor", proveedor.get());
        return "admin-proveedor-form";
    }

    @PostMapping("/proveedores/eliminar/{id}")
    public String deleteProveedor(@PathVariable String id, RedirectAttributes redirectAttributes) {
        proveedorService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Proveedor eliminado exitosamente.");
        return "redirect:/admin/proveedores";
    }

    @GetMapping("/historial-ofertas")
    public String historialOfertas(Model model) {
        List<Product> todos = productService.findAll();

        List<Product> acceptedOffers = todos.stream()
            .filter(p -> "ACEPTADA".equals(p.getEstadoAdmin()))
            .sorted((a, b) -> {
                if (a.getFechaOferta() == null && b.getFechaOferta() == null) return 0;
                if (a.getFechaOferta() == null) return 1;
                if (b.getFechaOferta() == null) return -1;
                return b.getFechaOferta().compareTo(a.getFechaOferta());
            })
            .collect(Collectors.toList());

        List<Product> rejectedOffers = todos.stream()
            .filter(p -> "RECHAZADA".equals(p.getEstadoAdmin()))
            .sorted((a, b) -> {
                if (a.getFechaOferta() == null && b.getFechaOferta() == null) return 0;
                if (a.getFechaOferta() == null) return 1;
                if (b.getFechaOferta() == null) return -1;
                return b.getFechaOferta().compareTo(a.getFechaOferta());
            })
            .collect(Collectors.toList());

        model.addAttribute("acceptedOffers", acceptedOffers);
        model.addAttribute("rejectedOffers", rejectedOffers);
        return "admin-historial-ofertas";
    }

}