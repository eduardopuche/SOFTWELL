package com.softwell.softwell.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import java.time.LocalDateTime;

@Document(collection = "products")
public class Product {

    @Id
    private String id;

    private String nombre;
    private String categoria;
    private double precio;
    private String imagen;
    private boolean destacado;
    private String descripcion;

    private double costoProveedor;
    private String detallesVentaAdmin;
    private String estadoAdmin; // PENDIENTE, ACEPTADA, RECHAZADA
    private LocalDateTime fechaOferta;

    private int stock;          // Stock de la tienda (baja con ventas)
    private int stockProveedor; // Stock original enviado por el proveedor
    private int cantidadOfertada;

    @DBRef
    private Proveedor proveedor;

    public Product() {}

    public Product(String id, String nombre, String categoria, double precio, String imagen,
                   boolean destacado, String descripcion, int stock, double costoProveedor,
                   String detallesVentaAdmin, String estadoAdmin) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.imagen = imagen;
        this.destacado = destacado;
        this.descripcion = descripcion;
        this.stock = stock;
        this.costoProveedor = costoProveedor;
        this.detallesVentaAdmin = detallesVentaAdmin;
        this.estadoAdmin = estadoAdmin;
    }

    public Product(String nombre, String categoria, double precio, String imagen,
                   boolean destacado, String descripcion, int stock, double costoProveedor,
                   String detallesVentaAdmin, String estadoAdmin) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.imagen = imagen;
        this.destacado = destacado;
        this.descripcion = descripcion;
        this.stock = stock;
        this.costoProveedor = costoProveedor;
        this.detallesVentaAdmin = detallesVentaAdmin;
        this.estadoAdmin = estadoAdmin;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public boolean isDestacado() { return destacado; }
    public void setDestacado(boolean destacado) { this.destacado = destacado; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getCostoProveedor() { return costoProveedor; }
    public void setCostoProveedor(double costoProveedor) { this.costoProveedor = costoProveedor; }

    public String getDetallesVentaAdmin() { return detallesVentaAdmin; }
    public void setDetallesVentaAdmin(String detallesVentaAdmin) { this.detallesVentaAdmin = detallesVentaAdmin; }

    public String getEstadoAdmin() { return estadoAdmin; }
    public void setEstadoAdmin(String estadoAdmin) { this.estadoAdmin = estadoAdmin; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getStockProveedor() { return stockProveedor; }
    public void setStockProveedor(int stockProveedor) { this.stockProveedor = stockProveedor; }

    public int getCantidadOfertada() { return cantidadOfertada; }
    public void setCantidadOfertada(int cantidadOfertada) { this.cantidadOfertada = cantidadOfertada; }

    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }

    public LocalDateTime getFechaOferta() { return fechaOferta; }
    public void setFechaOferta(LocalDateTime fechaOferta) { this.fechaOferta = fechaOferta; }
}
