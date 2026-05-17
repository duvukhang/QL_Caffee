package com.example.demo.Service.SQL.Product;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Models.Product;
import com.example.demo.Models.SubCategory;
import com.example.demo.Repositories.ProductRepository;
import com.example.demo.Repositories.SubCategoryRepository;

@Service
public class SqlProductServiceImpl implements SqlProductService {

    private final ProductRepository productRepository;
    private final SubCategoryRepository subCategoryRepository;

    public SqlProductServiceImpl(ProductRepository productRepository, SubCategoryRepository subCategoryRepository) {
        this.productRepository = productRepository;
        this.subCategoryRepository = subCategoryRepository;
    }

    @Override
    @Transactional // Tự động Commit/Rollback
    public Product createProducts(Product spMoi, String imgPath) {
        SubCategory subCategory = subCategoryRepository.findById(spMoi.getSubcategory().getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục con"));

        spMoi.setProductId(generateId("SP"));
        spMoi.setImg(imgPath);
        spMoi.setSubcategory(subCategory); // JPA quản lý qua Object thay vì ID

        return productRepository.save(spMoi);
    }

    @Override
    @Transactional
    public int softDeleteProduct(String productId) {
        if (productId == null || productId.isEmpty())
            return 500;

        Product sp = productRepository.findById(productId).orElse(null);
        if (sp == null)
            return 404;

        sp.setStatus("Ngưng Kinh Doanh");
        productRepository.save(sp); // Save thay cho EntityState.Modified
        return 200;
    }

    private String generateId(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
