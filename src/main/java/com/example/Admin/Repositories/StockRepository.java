package com.example.Admin.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Admin.Models.Stock;

import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<Stock, Integer> { 
    
    List<Stock> findByInventory_Store_StoreId(String storeId);
}