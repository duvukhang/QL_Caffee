package com.example.demo.Controller.Admin;

import com.example.demo.DTOS.Request.CreateProductRequest;
import com.example.demo.Models.Product;
import com.example.demo.Models.SubCategory; 
import com.example.demo.Repositories.ProductRepository;
import com.example.demo.Service.ImgService.ImgService;
import com.example.demo.Service.SQL.Product.SqlProductService;

import jakarta.validation.Valid; 
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/Admin/Product")
@PreAuthorize("hasAuthority('Admin')") 
public class ProductController {

    private final SqlProductService sqlProductService;
    private final ImgService imgService;
    private final ProductRepository productRepository;

    public ProductController(SqlProductService sqlProductService, ImgService imgService, ProductRepository productRepository) {
        this.sqlProductService = sqlProductService;
        this.imgService = imgService;
        this.productRepository = productRepository;
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @ModelAttribute CreateProductRequest request) throws Exception {
        if (request.getFile() == null || request.getFile().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng tải lên hình ảnh sản phẩm");
        }
        
        String relativeFilePath = imgService.SaveImgIntoProject(request.getFile());
        Product sp = new Product();
        sp.setProductName(request.getProductname());
        sp.setPrice(request.getDonGia());
        sp.setDescription(request.getMota());

        SubCategory sub = new SubCategory();
        sub.setSubCategory(request.getDmid()); 
        sp.setSubcategory(sub);

        var response = sqlProductService.createProducts(sp, relativeFilePath);
        return ResponseEntity.ok(Map.of("message", "Tạo sản phẩm thành công", "data", response));
    }

    @PutMapping("/Update/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable String id, @RequestBody Map<String, Object> updateData) {
        Product existing = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        
        if (updateData.containsKey("productName")) existing.setProductName(updateData.get("productName").toString());
        if (updateData.containsKey("price")) existing.setPrice(new BigDecimal(updateData.get("price").toString()));
        if (updateData.containsKey("description")) existing.setDescription(updateData.get("description").toString());
        
        productRepository.save(existing);
        return ResponseEntity.ok(Map.of("message", "Cập nhật thành công"));
    }

    @PutMapping("/SoftDelete/{productId}")
    public ResponseEntity<?> deleteSoft(@PathVariable String productId) {
        sqlProductService.softDeleteProduct(productId);
        return ResponseEntity.ok(Map.of("message", "Xóa mềm thành công"));
    }
}