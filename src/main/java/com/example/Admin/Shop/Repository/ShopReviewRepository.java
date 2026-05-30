package com.example.Admin.Shop.Repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.Admin.Shop.Model.ShopProduct;
import com.example.Admin.Shop.Model.ShopReview;

public interface ShopReviewRepository extends JpaRepository<ShopReview, Long> {
    List<ShopReview> findByProductAndApprovedTrueOrderByCreatedAtDesc(ShopProduct product);

    List<ShopReview> findByApprovedFalseOrderByCreatedAtDesc();

    List<ShopReview> findAllByOrderByCreatedAtDesc();

    boolean existsByUser_IdAndProduct_Id(Long userId, Long productId);

    long countByProductAndApprovedTrue(ShopProduct product);

    @Query("select avg(r.rating) from ShopReview r where r.product = :product and r.approved = true")
    Double averageApprovedRatingByProduct(@Param("product") ShopProduct product);

    @Query("""
            select r.product.id as productId, avg(r.rating) as averageRating, count(r.id) as reviewCount
            from ShopReview r
            where r.approved = true and r.product.id in :productIds
            group by r.product.id
            """)
    List<ProductRatingSummary> summarizeApprovedRatings(@Param("productIds") Collection<Long> productIds);

    interface ProductRatingSummary {
        Long getProductId();

        Double getAverageRating();

        Long getReviewCount();
    }
}
