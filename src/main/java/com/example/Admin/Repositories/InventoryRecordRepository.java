package com.example.Admin.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Admin.Models.Inventoryrecord;

@Repository
public interface InventoryRecordRepository extends JpaRepository<Inventoryrecord, String> {
    // Model là Inventoryrecord, Khóa chính kiểu String (RecordsId)
}