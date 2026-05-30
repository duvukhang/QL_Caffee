package com.example.Admin.Shop.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Admin.Shop.Model.ShopBrand;

public interface ShopBrandRepository extends JpaRepository<ShopBrand, Long> {
    Optional<ShopBrand> findByNameIgnoreCase(String name);
}
