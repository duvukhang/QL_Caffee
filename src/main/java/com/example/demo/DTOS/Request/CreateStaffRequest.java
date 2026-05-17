package com.example.demo.DTOS.Request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

@Data
public class CreateStaffRequest {
    private String ten;
    private String cccd;
    private String diaChi;
    private String gendar; // Giữ nguyên chính tả Gendar của bạn
    private String email;
    private String phoneNum;
    private String roleId;
    private BigDecimal luong;
    private String cuaHangId;
    private String ngaySinh;
    private MultipartFile file;
}
