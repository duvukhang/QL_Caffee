package com.example.Admin.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.Admin.Models.Order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {

    // 1. Hàm cũ đã fix ở bước trước
    @Query("SELECT o FROM Order o WHERE o.recivingDate BETWEEN :start AND :end AND o.sysuser.store.storeId = :storeId")
    List<Order> findByRecivingDateBetweenAndSysUser_StoreId(
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end, 
            @Param("storeId") String storeId
    );
    List<Order> findByRecivingDateBetween(LocalDateTime start, LocalDateTime end);

    // 🛠️ 2. BỔ SUNG HÀM NÀY: Ép Spring Boot đi đúng đường biến thực thể sysuser viết thường chuẩn Java
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.sysuser.store.storeId = :storeId")
    List<Order> findByStatusAndSysUser_StoreId(
            @Param("status") String status, 
            @Param("storeId") String storeId
    );

    List<Order> findByCustomer_CustomerIdOrderByRecivingDateDesc(String customerId);

    @Query("select o from Order o left join fetch o.orderDetails od left join fetch od.product left join fetch o.customer c left join fetch c.customerDetail where o.orderId = :id")
    Optional<Order> findDetail(@Param("id") String id);
}
