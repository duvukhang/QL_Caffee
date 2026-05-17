package com.example.demo.DTOS.Respone;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class HoaDonResponse {
    private String tenSP;
    private BigDecimal donGia; // decimal -> BigDecimal
    private int soLuong;
}
