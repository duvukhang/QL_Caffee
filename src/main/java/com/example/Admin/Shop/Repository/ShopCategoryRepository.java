package com.example.Admin.Shop.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Admin.Shop.Model.ShopCategory;

public interface ShopCategoryRepository extends JpaRepository<ShopCategory, Long> {
    List<ShopCategory> findByActiveTrueOrderByNameAsc();

    Optional<ShopCategory> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
