package com.example.demo.Repositories;

import com.example.demo.Models.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<Stock, Integer> { // Hoặc <Stock, String> tùy thuộc vào kiểu ID của bảng Stock
    
    // ĐÃ THÊM: Hàm để WarehouseManagerController gọi lấy danh sách tồn kho theo mã chi nhánh
    List<Stock> findByInventory_StoreId(String storeId);
}