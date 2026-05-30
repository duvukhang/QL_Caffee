package com.example.Admin.Shop.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Admin.Shop.Model.ShopInventoryHistory;

public interface ShopInventoryHistoryRepository extends JpaRepository<ShopInventoryHistory, Long> {
    List<ShopInventoryHistory> findTop50ByOrderByCreatedAtDesc();
}
