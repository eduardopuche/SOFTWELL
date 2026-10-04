package com.softwell.softwell.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "testimonios")
public class Testimonio {

    @Id
    private String id;

    private String nombreCliente;
    private String texto;
    private String imagenCliente;
    private boolean visible = true;

    public Testimonio() {}

    public Testimonio(String nombreCliente, String texto, String imagenCliente, boolean visible) {
        this.nombreCliente = nombreCliente;
        this.texto = texto;
        this.imagenCliente = imagenCliente;
        this.visible = visible;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public String getImagenCliente() { return imagenCliente; }
    public void setImagenCliente(String imagenCliente) { this.imagenCliente = imagenCliente; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
}
