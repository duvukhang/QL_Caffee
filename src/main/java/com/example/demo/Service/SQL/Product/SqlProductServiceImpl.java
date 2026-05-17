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
    @Transactional 
    public Product createProducts(Product spMoi, String imgPath) {
        // Lấy đúng Khóa chính (SubCategory)
        SubCategory subCategory = subCategoryRepository.findById(spMoi.getSubcategory().getSubCategory())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã danh mục: " + spMoi.getSubcategory().getSubCategory()));

        spMoi.setProductId(generateId("SP"));
        spMoi.setImg(imgPath);
        spMoi.setStatus("Kinh doanh");
        spMoi.setSubcategory(subCategory); 

        return productRepository.save(spMoi);
    }

    @Override
    @Transactional
    public int softDeleteProduct(String productId) {
        if (productId == null || productId.isEmpty()) return 500;
        Product sp = productRepository.findById(productId).orElse(null);
        if (sp == null) return 404;
        
        sp.setStatus("Ngưng Kinh Doanh");
        productRepository.save(sp); 
        return 200;
    }

    private String generateId(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}