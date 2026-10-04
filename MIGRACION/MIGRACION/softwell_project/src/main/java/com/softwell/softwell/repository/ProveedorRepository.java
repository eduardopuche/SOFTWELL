package com.softwell.softwell.repository;

import com.softwell.softwell.model.Proveedor;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface ProveedorRepository extends MongoRepository<Proveedor, String> {
    Optional<Proveedor> findByEmail(String email);
}
