package com.example.demo.Controller.Admin;

// Thêm toàn bộ thư viện cần thiết ở đây
import com.example.demo.DTOS.Request.CreateProductRequest;
import com.example.demo.Models.Product;
import com.example.demo.Models.SubCategory; // IMPORT QUAN TRỌNG: Thêm SubCategory để map dữ liệu
import com.example.demo.Service.ImgService.ImgService;
import com.example.demo.Service.SQL.Product.SqlProductService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/Admin/Product")
@PreAuthorize("hasRole('ADMIN')")
public class ProductController {

    private final SqlProductService sqlProductService;
    private final ImgService imgService;

    public ProductController(SqlProductService sqlProductService, ImgService imgService) {
        this.sqlProductService = sqlProductService;
        this.imgService = imgService;
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@ModelAttribute CreateProductRequest request) throws Exception {
        if (request == null || request.getProductname() == null || request.getMota() == null
                || request.getDmid() == null) {
            throw new IllegalArgumentException("Missing Product");
        }
        if (request.getFile() == null || request.getFile().isEmpty()) {
            throw new IllegalArgumentException("Missing File Product IMG");
        }
        String relativeFilePath = imgService.SaveImgIntoProject(request.getFile());
        Product sp = new Product();
        sp.setProductName(request.getProductname());
        sp.setPrice(request.getDonGia());

        // NẾU VẪN BÁO LỖI DÒNG NÀY: Xóa chữ 'Decription' đi, gõ 'sp.set' rồi bấm Ctrl +
        // Space trong VS Code để chọn đúng tên hàm (có thể là setDescription)
        sp.setDescription(request.getMota());

        // ĐÃ FIX: Sử dụng Object SubCategory để truyền vào Product thay vì dùng String
        // ID thô
        SubCategory sub = new SubCategory();
        sub.setCategoryId(request.getDmid());
        sp.setSubcategory(sub);

        // ĐÃ FIX: Chữ 'd' trong createProducts đã được viết thường cho khớp với file
        // Service
        var response = sqlProductService.createProducts(sp, relativeFilePath);

        if (response == null)
            throw new RuntimeException("Failed to create products with img");
        return ResponseEntity.ok(Map.of("message", "Create Successfull", "data", response));
    }

    @PutMapping("/SoftDelete/{productId}")
    public ResponseEntity<?> deleteSoft(@PathVariable String productId) {
        if (productId == null || productId.isEmpty())
            throw new IllegalArgumentException("Missing Param ProductId");
        int status = sqlProductService.softDeleteProduct(productId);
        if (status == 404)
            throw new RuntimeException("Product Not Found");
        if (status == 500)
            throw new RuntimeException("Can't soft Delete this Product");
        return ResponseEntity.ok(Map.of("message", "Successfully Soft Delete Product"));
    }
}