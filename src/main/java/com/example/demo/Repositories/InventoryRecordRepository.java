package com.example.demo.Repositories;

import com.example.demo.Models.Inventoryrecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRecordRepository extends JpaRepository<Inventoryrecord, String> {
    // Model là Inventoryrecord, Khóa chính kiểu String (RecordsId)
}