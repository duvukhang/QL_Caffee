package com.example.demo.Repositories;

import com.example.demo.Models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {

    // 1. Hàm cũ đã fix ở bước trước
    @Query("SELECT o FROM Order o WHERE o.recivingDate BETWEEN :start AND :end AND o.sysuser.store.storeId = :storeId")
    List<Order> findByRecivingDateBetweenAndSysUser_StoreId(
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end, 
            @Param("storeId") String storeId
    );

    // 🛠️ 2. BỔ SUNG HÀM NÀY: Ép Spring Boot đi đúng đường biến thực thể sysuser viết thường chuẩn Java
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.sysuser.store.storeId = :storeId")
    List<Order> findByStatusAndSysUser_StoreId(
            @Param("status") String status, 
            @Param("storeId") String storeId
    );
}