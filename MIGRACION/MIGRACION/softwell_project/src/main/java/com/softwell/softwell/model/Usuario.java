package com.softwell.softwell.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Document(collection = "usuarios")
public class Usuario {

    @Id
    private String id;

    private String email;
    private String password;
    private String nombre;
    private String rol;
    private String telefono;
    private String direccion;
    private String fechaRegistro;
    private String fotoPerfil;
    private Boolean activo;
    private Double latitud;
    private Double longitud;

    public Usuario() {
        this.activo = true;
        this.fechaRegistro = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public Usuario(String email, String password, String nombre, String telefono, String direccion) {
        this.email = email;
        this.password = password;
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
        this.activo = true;
        this.fechaRegistro = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(String fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public boolean esAdmin() { return "admin".equalsIgnoreCase(this.rol); }
    public boolean esCliente() { return "cliente".equalsIgnoreCase(this.rol); }
    public boolean esProveedor() { return "proveedor".equalsIgnoreCase(this.rol); }
    public boolean estaActivo() { return this.activo != null && this.activo; }

    @Override
    public String toString() {
        return "Usuario{id=" + id + ", email='" + email + "', nombre='" + nombre +
               "', rol='" + rol + "', activo=" + activo + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return id != null && id.equals(usuario.id);
    }

    @Override
    public int hashCode() { return id != null ? id.hashCode() : 0; }
}
