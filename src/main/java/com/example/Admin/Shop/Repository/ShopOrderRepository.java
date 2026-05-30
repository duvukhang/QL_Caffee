package com.example.Admin.Shop.Repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.example.Admin.Shop.Model.ShopOrder;
import com.example.Admin.Shop.Model.ShopOrderStatus;
import com.example.Admin.Shop.Model.ShopUser;

public interface ShopOrderRepository extends JpaRepository<ShopOrder, Long>, JpaSpecificationExecutor<ShopOrder> {
    List<ShopOrder> findByUserOrderByCreatedAtDesc(ShopUser user);

    List<ShopOrder> findTop10ByOrderByCreatedAtDesc();

    long countByStatus(ShopOrderStatus status);

    @Query("select coalesce(sum(o.totalAmount), 0) from ShopOrder o where o.status = com.example.Admin.Shop.Model.ShopOrderStatus.COMPLETED")
    java.math.BigDecimal sumCompletedRevenue();

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
