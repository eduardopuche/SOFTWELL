package com.softwell.softwell.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "pedidos")
public class Pedido {

    @Id
    private String id;

    private String nombreCliente;
    private String emailCliente;
    private String telefonoCliente;
    private String direccionCliente;

    private String usuarioId;

    private LocalDateTime fechaCreacion;
    private String estado;

    private double total;
    private String descripcionItems;
    private String imagenesItems;
    private double iva;
    private double costoEnvio;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getEmailCliente() { return emailCliente; }
    public void setEmailCliente(String emailCliente) { this.emailCliente = emailCliente; }

    public String getTelefonoCliente() { return telefonoCliente; }
    public void setTelefonoCliente(String telefonoCliente) { this.telefonoCliente = telefonoCliente; }

    public String getDireccionCliente() { return direccionCliente; }
    public void setDireccionCliente(String direccionCliente) { this.direccionCliente = direccionCliente; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getDescripcionItems() { return descripcionItems; }
    public void setDescripcionItems(String descripcionItems) { this.descripcionItems = descripcionItems; }

    public String getImagenesItems() { return imagenesItems; }
    public void setImagenesItems(String imagenesItems) { this.imagenesItems = imagenesItems; }

    public double getIva() { return iva; }
    public void setIva(double iva) { this.iva = iva; }

    public double getCostoEnvio() { return costoEnvio; }
    public void setCostoEnvio(double costoEnvio) { this.costoEnvio = costoEnvio; }
}
