package com.example.Admin.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.Admin.Models.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {

    List<Product> findTop3ByOrderByPriceDesc();

    List<Product> findTop4BySubcategory_SubCategoryAndProductIdNot(String subCategory, String productId);

    List<Product> findTop4ByOrderByProductIdDesc();

    @Query("select p from Product p left join fetch p.subcategory s left join fetch s.category")
    List<Product> findAllWithCategory();
}
