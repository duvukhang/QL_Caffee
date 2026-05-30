package com.example.Admin.DTOS.Request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateUserRequest {
    private String ten;
    private String diaChi;
    private String cccd;
    private String vtri;
    private String ngaySinh;
    private BigDecimal luong;
}
