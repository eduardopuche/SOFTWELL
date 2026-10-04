package com.softwell.softwell.service;

import com.softwell.softwell.model.Proveedor;
import com.softwell.softwell.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    public List<Proveedor> findAll() { return proveedorRepository.findAll(); }

    public Optional<Proveedor> findById(String id) { return proveedorRepository.findById(id); }

    public Proveedor save(Proveedor proveedor) { return proveedorRepository.save(proveedor); }

    public void deleteById(String id) { proveedorRepository.deleteById(id); }

    public Optional<Proveedor> findByEmail(String email) { return proveedorRepository.findByEmail(email); }
}
