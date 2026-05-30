package com.example.Admin.Shop.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Admin.Shop.Model.ShopOrderItem;

public interface ShopOrderItemRepository extends JpaRepository<ShopOrderItem, Long> {
    boolean existsByOrder_User_IdAndProduct_IdAndOrder_Status(Long userId, Long productId, com.example.Admin.Shop.Model.ShopOrderStatus status);
}
