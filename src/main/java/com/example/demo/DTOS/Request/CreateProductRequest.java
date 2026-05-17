package com.example.demo.DTOS.Request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

@Data
public class CreateProductRequest {
    private String productname;
    private String dmid; // Đổi DMID thành chữ thường để chuẩn JSON/Java naming
    private String mota;
    private BigDecimal donGia; // decimal trong C# -> BigDecimal trong Java
    private MultipartFile file; // IFormFile -> MultipartFile
}
