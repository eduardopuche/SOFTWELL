package com.softwell.softwell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PedidoTest {

    private Pedido pedido;

    @BeforeEach
    void setUp() {
        pedido = new Pedido();
    }

    @Test
    void probarId() {
        pedido.setId("PED001");

        assertEquals("PED001", pedido.getId());
    }

    @Test
    void probarDatosCliente() {
        pedido.setNombreCliente("Eduardo");
        pedido.setEmailCliente("eduardo@gmail.com");
        pedido.setTelefonoCliente("3001234567");
        pedido.setDireccionCliente("Cartagena");

        assertEquals("Eduardo", pedido.getNombreCliente());
        assertEquals("eduardo@gmail.com", pedido.getEmailCliente());
        assertEquals("3001234567", pedido.getTelefonoCliente());
        assertEquals("Cartagena", pedido.getDireccionCliente());
    }

    @Test
    void probarUsuarioId() {
        pedido.setUsuarioId("USR001");

        assertEquals("USR001", pedido.getUsuarioId());
    }

    @Test
    void probarFechaCreacion() {
        LocalDateTime fecha = LocalDateTime.of(
                2026, 10, 20, 10, 30
        );

        pedido.setFechaCreacion(fecha);

        assertEquals(fecha, pedido.getFechaCreacion());
    }

    @Test
    void probarEstado() {
        pedido.setEstado("Pendiente");

        assertEquals("Pendiente", pedido.getEstado());
    }

    @Test
    void probarTotal() {
        pedido.setTotal(250000.0);

        assertEquals(250000.0, pedido.getTotal(), 0.001);
    }

    @Test
    void probarDescripcionItems() {
        pedido.setDescripcionItems("Anillo de oro");

        assertEquals(
                "Anillo de oro",
                pedido.getDescripcionItems()
        );
    }

    @Test
    void probarImagenesItems() {
        pedido.setImagenesItems("anillo.jpg");

        assertEquals(
                "anillo.jpg",
                pedido.getImagenesItems()
        );
    }

    @Test
    void probarIva() {
        pedido.setIva(47500.0);

        assertEquals(47500.0, pedido.getIva(), 0.001);
    }

    @Test
    void probarCostoEnvio() {
        pedido.setCostoEnvio(15000.0);

        assertEquals(15000.0, pedido.getCostoEnvio(), 0.001);
    }

    @Test
    void probarPedidoCompleto() {
        pedido.setId("PED002");
        pedido.setNombreCliente("Eduardo");
        pedido.setEmailCliente("eduardo@gmail.com");
        pedido.setTelefonoCliente("3001234567");
        pedido.setDireccionCliente("Cartagena");
        pedido.setUsuarioId("USR001");
        pedido.setFechaCreacion(LocalDateTime.of(
                2026, 10, 20, 10, 30
        ));
        pedido.setEstado("Pendiente");
        pedido.setTotal(250000.0);
        pedido.setDescripcionItems("Anillo de oro");
        pedido.setImagenesItems("anillo.jpg");
        pedido.setIva(47500.0);
        pedido.setCostoEnvio(15000.0);

        assertAll(
                () -> assertEquals("PED002", pedido.getId()),
                () -> assertEquals("Eduardo", pedido.getNombreCliente()),
                () -> assertEquals("eduardo@gmail.com", pedido.getEmailCliente()),
                () -> assertEquals("3001234567", pedido.getTelefonoCliente()),
                () -> assertEquals("Cartagena", pedido.getDireccionCliente()),
                () -> assertEquals("USR001", pedido.getUsuarioId()),
                () -> assertEquals("Pendiente", pedido.getEstado()),
                () -> assertEquals(250000.0, pedido.getTotal(), 0.001),
                () -> assertEquals("Anillo de oro", pedido.getDescripcionItems()),
                () -> assertEquals("anillo.jpg", pedido.getImagenesItems()),
                () -> assertEquals(47500.0, pedido.getIva(), 0.001),
                () -> assertEquals(15000.0, pedido.getCostoEnvio(), 0.001)
        );
    }
}
