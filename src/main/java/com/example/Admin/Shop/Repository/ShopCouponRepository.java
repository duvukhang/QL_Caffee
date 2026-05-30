package com.example.Admin.Shop.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Admin.Shop.Model.ShopCoupon;

public interface ShopCouponRepository extends JpaRepository<ShopCoupon, Long> {
    Optional<ShopCoupon> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    List<ShopCoupon> findByActiveTrueOrderByCodeAsc();
}
