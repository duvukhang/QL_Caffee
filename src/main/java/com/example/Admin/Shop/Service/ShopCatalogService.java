package com.example.Admin.Shop.Service;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.Admin.Shop.Model.ShopProduct;
import com.example.Admin.Shop.Repository.ShopProductRepository;

import jakarta.persistence.criteria.Predicate;

@Service
public class ShopCatalogService {
    private final ShopProductRepository productRepository;

    public ShopCatalogService(ShopProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Page<ShopProduct> search(String keyword, Long categoryId, Long brandId, BigDecimal minPrice, BigDecimal maxPrice,
            String sort, int page, int size) {
        Specification<ShopProduct> spec = (root, query, cb) -> {
            Predicate predicate = cb.isTrue(root.get("active"));
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("name")), like));
            }
            if (categoryId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("category").get("id"), categoryId));
            }
            if (brandId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("brand").get("id"), brandId));
            }
            if (minPrice != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            return predicate;
        };
        return productRepository.findAll(spec, PageRequest.of(Math.max(page, 0), size, sort(sort)));
    }

    private Sort sort(String sort) {
        return switch (sort == null ? "" : sort) {
            case "priceAsc" -> Sort.by("price").ascending();
            case "priceDesc" -> Sort.by("price").descending();
            case "popular" -> Sort.by(Sort.Order.desc("featured"), Sort.Order.desc("createdAt"));
            default -> Sort.by("createdAt").descending();
        };
    }
}
