package com.softwell.softwell.service;

import com.softwell.softwell.model.CartItem;
import com.softwell.softwell.model.Usuario;
import com.softwell.softwell.model.Pedido;
import com.softwell.softwell.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductService productService;

    public PedidoService(PedidoRepository pedidoRepository, ProductService productService) {
        this.pedidoRepository = pedidoRepository;
        this.productService = productService;
    }

    public Pedido crearPedido(List<CartItem> cartItems, String nombre, String email, String telefono,
                              String direccion, Usuario usuario, double finalTotal, double iva, double costoEnvio) {
        Pedido pedido = new Pedido();
        pedido.setNombreCliente(nombre);
        pedido.setEmailCliente(email);
        pedido.setTelefonoCliente(telefono);
        pedido.setDireccionCliente(direccion);
        pedido.setFechaCreacion(LocalDateTime.now());
        pedido.setEstado("PENDIENTE");
        if (usuario != null) pedido.setUsuarioId(usuario.getId());

        pedido.setTotal(finalTotal);
        pedido.setIva(iva);
        pedido.setCostoEnvio(costoEnvio);

        StringBuilder resumen = new StringBuilder();
        StringBuilder imagenes = new StringBuilder();

        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            resumen.append(item.getProduct().getNombre()).append(" (x").append(item.getCantidad()).append(")");
            imagenes.append(item.getProduct().getImagen());
            if (i < cartItems.size() - 1) { resumen.append(", "); imagenes.append(","); }

            final int qty = item.getCantidad();
            productService.findById(item.getProduct().getId()).ifPresent(p -> {
                p.setStock(p.getStock() - qty);
                p.setStockProveedor(p.getStockProveedor() - qty);
                productService.save(p);
            });
        }
        pedido.setDescripcionItems(resumen.toString());
        pedido.setImagenesItems(imagenes.toString());
        return pedidoRepository.save(pedido);
    }

    public void procesarPagoPedido(String pedidoId, boolean aprobado) {
        pedidoRepository.findById(pedidoId).ifPresent(pedido -> {
            pedido.setEstado(aprobado ? "PAGADO" : "RECHAZADO");
            pedidoRepository.save(pedido);
        });
    }

    public List<Pedido> findAll() { return pedidoRepository.findAll(); }

    public Optional<Pedido> findById(String id) { return pedidoRepository.findById(id); }

    public List<Pedido> findByUsuarioId(String usuarioId) {
        return pedidoRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
    }
}
