package com.softwell.softwell.service;

import com.softwell.softwell.repository.ProductRepository;
import com.softwell.softwell.model.Product;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() { return productRepository.findAll(); }

    public Optional<Product> findById(String id) { return productRepository.findById(id); }

    public List<Product> findDestacados(int limit) {
        return productRepository.findTop6ByDestacadoOrderByIdDesc(true).stream()
                .filter(p -> p.getStock() > 0 && (p.getEstadoAdmin() == null || "ACEPTADA".equals(p.getEstadoAdmin())))
                .collect(Collectors.toList());
    }

    public List<Product> findNuevos(int limit) {
        return productRepository.findTop4ByOrderByIdDesc().stream()
                .filter(p -> p.getStock() > 0 && (p.getEstadoAdmin() == null || "ACEPTADA".equals(p.getEstadoAdmin())))
                .collect(Collectors.toList());
    }

    public List<Product> findByCategoria(String categoria) {
        List<Product> products = (categoria == null || categoria.isEmpty() || categoria.equalsIgnoreCase("all"))
                ? productRepository.findAll()
                : productRepository.findByCategoria(categoria);
        return products.stream()
                .filter(p -> p.getStock() > 0 && (p.getEstadoAdmin() == null || "ACEPTADA".equals(p.getEstadoAdmin())))
                .collect(Collectors.toList());
    }

    public List<Product> findByProveedorId(String proveedorId) {
        return productRepository.findByProveedorId(proveedorId);
    }

    public void reducirStock(String productId, int cantidad) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            if (product.getStock() >= cantidad) {
                product.setStock(product.getStock() - cantidad);
                productRepository.save(product);
            } else {
                throw new RuntimeException("Stock insuficiente para el producto: " + product.getNombre());
            }
        }
    }

    public void aumentarStock(String productId, int cantidad) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setStock(product.getStock() + cantidad);
            productRepository.save(product);
        }
    }

    public Product save(Product product) { return productRepository.save(product); }

    public void deleteById(String id) { productRepository.deleteById(id); }
}
