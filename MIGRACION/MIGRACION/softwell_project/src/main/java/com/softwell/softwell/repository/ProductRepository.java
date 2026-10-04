package com.softwell.softwell.repository;

import com.softwell.softwell.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findTop6ByDestacadoOrderByIdDesc(boolean destacado);
    List<Product> findTop4ByOrderByIdDesc();
    List<Product> findByCategoria(String categoria);
    List<Product> findByProveedorId(String proveedorId);
}
