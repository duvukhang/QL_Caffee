package com.example.Admin.Shop.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Admin.Shop.Model.ShopCart;
import com.example.Admin.Shop.Model.ShopUser;

public interface ShopCartRepository extends JpaRepository<ShopCart, Long> {
    Optional<ShopCart> findByUser(ShopUser user);
}
