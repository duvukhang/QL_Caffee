package com.example.Admin.Shop.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Admin.Shop.Model.ShopCart;
import com.example.Admin.Shop.Model.ShopCartItem;
import com.example.Admin.Shop.Model.ShopProduct;

public interface ShopCartItemRepository extends JpaRepository<ShopCartItem, Long> {
    Optional<ShopCartItem> findByCartAndProduct(ShopCart cart, ShopProduct product);
}
