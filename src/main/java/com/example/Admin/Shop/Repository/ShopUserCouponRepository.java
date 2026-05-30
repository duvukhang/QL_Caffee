package com.example.Admin.Shop.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Admin.Shop.Model.ShopCoupon;
import com.example.Admin.Shop.Model.ShopUser;
import com.example.Admin.Shop.Model.ShopUserCoupon;

public interface ShopUserCouponRepository extends JpaRepository<ShopUserCoupon, Long> {
    List<ShopUserCoupon> findByUserOrderByAssignedAtDesc(ShopUser user);

    Optional<ShopUserCoupon> findByUserAndCoupon(ShopUser user, ShopCoupon coupon);

    boolean existsByUserAndCoupon(ShopUser user, ShopCoupon coupon);
}
