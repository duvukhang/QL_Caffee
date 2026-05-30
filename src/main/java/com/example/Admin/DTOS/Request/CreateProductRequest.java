package com.example.Admin.DTOS.Request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class CreateProductRequest {
    
    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String productname;

    @NotBlank(message = "Danh mục không được để trống")
    private String dmid; 

    private String mota;

    @NotNull(message = "Giá tiền không được để trống")
    @Min(value = 0, message = "Giá tiền không được là số âm")
    private BigDecimal donGia; 

    private MultipartFile file; 
}