package com.example.Admin.Shop.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.Admin.Shop.Model.ShopOrder;
import com.example.Admin.Shop.Model.ShopOrderStatus;
import com.example.Admin.Shop.Model.ShopUser;

public interface ShopOrderRepository extends JpaRepository<ShopOrder, Long>, JpaSpecificationExecutor<ShopOrder> {
    List<ShopOrder> findByUserOrderByCreatedAtDesc(ShopUser user);

    List<ShopOrder> findTop10ByOrderByCreatedAtDesc();

    List<ShopOrder> findTop10ByStatusOrderByCreatedAtDesc(ShopOrderStatus status);

    boolean existsByOrderCode(String orderCode);

    long countByStatus(ShopOrderStatus status);

    @Query("select coalesce(sum(o.totalAmount), 0) from ShopOrder o where o.status = com.example.Admin.Shop.Model.ShopOrderStatus.COMPLETED")
    BigDecimal sumCompletedRevenue();

    @Query("""
            select coalesce(sum(o.totalAmount), 0)
            from ShopOrder o
            where o.status = :status
              and o.createdAt >= :start
              and o.createdAt < :end
            """)
    BigDecimal sumRevenueByStatusAndCreatedAtBetween(@Param("status") ShopOrderStatus status,
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    long countByStatusAndCreatedAtBetween(ShopOrderStatus status, LocalDateTime start, LocalDateTime end);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
