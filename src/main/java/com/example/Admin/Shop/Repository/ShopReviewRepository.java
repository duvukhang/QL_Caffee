package com.example.Admin.Shop.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Admin.Shop.Model.ShopProduct;
import com.example.Admin.Shop.Model.ShopReview;

public interface ShopReviewRepository extends JpaRepository<ShopReview, Long> {
    List<ShopReview> findByProductAndApprovedTrueOrderByCreatedAtDesc(ShopProduct product);

    List<ShopReview> findByApprovedFalseOrderByCreatedAtDesc();

    List<ShopReview> findAllByOrderByCreatedAtDesc();
}
