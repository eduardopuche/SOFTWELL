package com.softwell.softwell.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import com.softwell.softwell.model.CartItem;
import com.softwell.softwell.model.Product;

@Service
@SessionScope
public class CartService {

    private final List<CartItem> items = new ArrayList<>();

    public void addItem(Product product, int cantidad) {
        if (product.getStock() <= 0) return;

        Optional<CartItem> existingItem = items.stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setCantidad(existingItem.get().getCantidad() + cantidad);
        } else {
            items.add(new CartItem(product, cantidad));
        }
    }

    public void removeItem(String productId) {
        items.removeIf(item -> item.getProduct().getId().equals(productId));
    }

    public void updateQuantity(String productId, int cantidad) {
        items.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresent(item -> item.setCantidad(cantidad));
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items);
    }

    public double getTotal() {
        return items.stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
    }

    public int getTotalItems() {
        return items.stream()
                .mapToInt(CartItem::getCantidad)
                .sum();
    }

    public void clear() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}