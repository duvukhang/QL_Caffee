package com.example.demo.Repositories;

import com.example.demo.Models.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<Stock, Integer> { 
    
    List<Stock> findByInventory_Store_StoreId(String storeId);
}