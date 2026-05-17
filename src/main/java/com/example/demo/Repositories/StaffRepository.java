package com.example.demo.Repositories;

import com.example.demo.Models.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface StaffRepository extends JpaRepository<Staff, String> {

    // 🛠️ ĐÃ FIX: Ép câu lệnh JPQL đi đúng thuộc tính storeId nằm trong thực thể Store
    @Query("SELECT s FROM Staff s WHERE s.store.storeId = :storeId")
    List<Staff> findByStoreId(@Param("storeId") String storeId);
}