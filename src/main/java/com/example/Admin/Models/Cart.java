package com.example.Admin.Models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Cart implements Serializable {

    private List<CartItem> items = new ArrayList<>();

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items == null ? new ArrayList<>() : items;
    }

    public int quantity() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public BigDecimal totalMoney() {
        return items.stream()
                .map(CartItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addItem(CartItem item) {
        if (item == null || item.getProductId() == null) {
            return;
        }

        items.stream()
                .filter(existing -> existing.getProductId().equals(item.getProductId()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.setQuantity(existing.getQuantity() + item.getQuantity()),
                        () -> items.add(item)
                );
    }

    public void removeItem(String id) {
        items.removeIf(item -> item.getProductId() != null && item.getProductId().equals(id));
    }

    public void updateQuantity(String id, int newQuantity) {
        if (newQuantity <= 0) {
            removeItem(id);
            return;
        }

        items.stream()
                .filter(item -> item.getProductId() != null && item.getProductId().equals(id))
                .findFirst()
                .ifPresent(item -> item.setQuantity(newQuantity));
    }
}
