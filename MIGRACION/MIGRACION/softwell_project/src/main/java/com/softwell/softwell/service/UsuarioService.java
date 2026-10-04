package com.softwell.softwell.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.softwell.softwell.model.Usuario;
import com.softwell.softwell.model.Proveedor;
import com.softwell.softwell.repository.UsuarioRepository;
import com.softwell.softwell.repository.ProveedorRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuario registrarUsuario(String email, String password, String nombre, String telefono,
                                    String direccion, Double latitud, Double longitud, String rol) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new RuntimeException("El email ya está registrado");
        }
        if (email == null || email.trim().isEmpty()) throw new RuntimeException("El email es requerido");
        if (password == null || password.trim().isEmpty()) throw new RuntimeException("La contraseña es requerida");
        if (nombre == null || nombre.trim().isEmpty()) throw new RuntimeException("El nombre es requerido");

        Usuario usuario = new Usuario(email, passwordEncoder.encode(password), nombre, telefono, direccion);
        usuario.setActivo(true);
        if (latitud != null) usuario.setLatitud(latitud);
        if (longitud != null) usuario.setLongitud(longitud);

        String finalRol;
        if (usuarioRepository.count() == 0) {
            finalRol = "admin";
        } else {
            finalRol = (rol != null && !rol.isEmpty()) ? rol : "cliente";
        }
        usuario.setRol(finalRol);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        if ("proveedor".equalsIgnoreCase(finalRol)) {
            if (proveedorRepository.findByEmail(usuarioGuardado.getEmail()).isEmpty()) {
                Proveedor nuevoP = new Proveedor();
                nuevoP.setEmail(usuarioGuardado.getEmail());
                nuevoP.setNombre(usuarioGuardado.getNombre());
                nuevoP.setTelefono(usuarioGuardado.getTelefono());
                nuevoP.setDireccion(usuarioGuardado.getDireccion());
                proveedorRepository.save(nuevoP);
            }
        }
        return usuarioGuardado;
    }

    public Usuario validarLogin(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) return null;
        Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
        if (usuario.isPresent()) {
            Usuario u = usuario.get();
            if (passwordEncoder.matches(password, u.getPassword()) && u.estaActivo()) return u;
        }
        return null;
    }

    public Usuario obtenerPorId(String id) {
        if (id == null || id.isBlank()) return null;
        return usuarioRepository.findById(id).orElse(null);
    }

    public Usuario obtenerPorEmail(String email) {
        if (email == null || email.trim().isEmpty()) return null;
        return usuarioRepository.findByEmail(email).orElse(null);
    }

    public Usuario actualizarRol(String id, String nuevoRol) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        if (usuario.isPresent()) {
            Usuario u = usuario.get();
            u.setRol(nuevoRol);
            if ("proveedor".equalsIgnoreCase(nuevoRol)) {
                if (proveedorRepository.findByEmail(u.getEmail()).isEmpty()) {
                    Proveedor nuevoP = new Proveedor();
                    nuevoP.setEmail(u.getEmail());
                    nuevoP.setNombre(u.getNombre());
                    nuevoP.setTelefono(u.getTelefono());
                    nuevoP.setDireccion(u.getDireccion());
                    proveedorRepository.save(nuevoP);
                }
            }
            return usuarioRepository.save(u);
        }
        return null;
    }

    public Usuario actualizarUsuario(String id, String email, String nombre, String telefono,
                                     String direccion, String fotoPerfil, Double latitud, Double longitud) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        if (usuario.isPresent()) {
            Usuario u = usuario.get();
            String emailActual = u.getEmail();
            Optional<Proveedor> provOpt = Optional.empty();
            if (u.esProveedor()) provOpt = proveedorRepository.findByEmail(emailActual);

            if (nombre != null && !nombre.trim().isEmpty()) u.setNombre(nombre);
            if (email != null && !email.trim().isEmpty() && !u.getEmail().equals(email)) {
                if (usuarioRepository.existsByEmail(email)) throw new RuntimeException("El nuevo email ya está registrado.");
                u.setEmail(email);
            }
            if (telefono != null && !telefono.trim().isEmpty()) u.setTelefono(telefono);
            if (direccion != null && !direccion.trim().isEmpty()) u.setDireccion(direccion);
            if (fotoPerfil != null && !fotoPerfil.trim().isEmpty()) u.setFotoPerfil(fotoPerfil);
            if (latitud != null) u.setLatitud(latitud);
            if (longitud != null) u.setLongitud(longitud);

            Usuario usuarioGuardado = usuarioRepository.save(u);

            if (provOpt.isPresent()) {
                Proveedor p = provOpt.get();
                p.setNombre(u.getNombre());
                p.setEmail(u.getEmail());
                p.setTelefono(u.getTelefono());
                p.setDireccion(u.getDireccion());
                proveedorRepository.save(p);
            } else if (u.esProveedor()) {
                Proveedor nuevoP = new Proveedor();
                nuevoP.setNombre(u.getNombre());
                nuevoP.setEmail(u.getEmail());
                nuevoP.setTelefono(u.getTelefono());
                nuevoP.setDireccion(u.getDireccion());
                proveedorRepository.save(nuevoP);
            }
            return usuarioGuardado;
        }
        return null;
    }

    public Usuario cambiarPassword(String id, String passwordActual, String passwordNueva) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        if (usuario.isPresent()) {
            Usuario u = usuario.get();
            if (!passwordEncoder.matches(passwordActual, u.getPassword()))
                throw new RuntimeException("La contraseña actual es incorrecta");
            if (passwordNueva == null || passwordNueva.trim().isEmpty())
                throw new RuntimeException("La nueva contraseña es requerida");
            u.setPassword(passwordEncoder.encode(passwordNueva));
            return usuarioRepository.save(u);
        }
        throw new RuntimeException("Usuario no encontrado");
    }

    public void desactivarUsuario(String id) {
        usuarioRepository.findById(id).ifPresent(u -> { u.setActivo(false); usuarioRepository.save(u); });
    }

    public void activarUsuario(String id) {
        usuarioRepository.findById(id).ifPresent(u -> { u.setActivo(true); usuarioRepository.save(u); });
    }

    public void eliminarUsuario(String id) { usuarioRepository.deleteById(id); }

    public void save(Usuario usuario) { usuarioRepository.save(usuario); }

    public List<Usuario> obtenerTodos() { return usuarioRepository.findAll(); }

    public List<Usuario> obtenerTodosLosClientes() {
        return usuarioRepository.findAll().stream().filter(Usuario::esCliente).collect(Collectors.toList());
    }

    public List<Usuario> obtenerTodosLosAdmins() {
        return usuarioRepository.findAll().stream().filter(Usuario::esAdmin).collect(Collectors.toList());
    }

    public boolean emailExiste(String email) { return usuarioRepository.existsByEmail(email); }

    public int contarUsuarios() { return (int) usuarioRepository.count(); }

    public int contarClientes() {
        return (int) usuarioRepository.findAll().stream().filter(Usuario::esCliente).count();
    }

    public int contarProveedoresUsuarios() {
        return (int) usuarioRepository.findAll().stream().filter(Usuario::esProveedor).count();
    }
}
