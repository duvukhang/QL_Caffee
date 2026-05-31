package com.example.Admin.Shop.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.example.Admin.Shop.Model.ShopProduct;
import com.example.Admin.Shop.Model.ShopCategory;

public interface ShopProductRepository extends JpaRepository<ShopProduct, Long>, JpaSpecificationExecutor<ShopProduct> {
    Optional<ShopProduct> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<ShopProduct> findTop8ByActiveTrueOrderByCreatedAtDesc();

    List<ShopProduct> findTop8ByActiveTrueAndFeaturedTrueOrderByCreatedAtDesc();

    List<ShopProduct> findTop8ByActiveTrueAndSaleProductTrueOrderByCreatedAtDesc();

    List<ShopProduct> findByActiveTrueOrderByNameAsc();

    List<ShopProduct> findByQuantityLessThanOrderByQuantityAsc(int quantity);

    long countByActiveTrue();

    boolean existsByCategory(ShopCategory category);

    @Query("select p from ShopProduct p left join fetch p.images where p.id = :id")
    Optional<ShopProduct> findDetailById(Long id);

    @Query("select distinct p from ShopProduct p left join fetch p.images left join fetch p.category where p.active = true order by p.name")
    List<ShopProduct> findActiveWithImagesOrderByNameAsc();
}
