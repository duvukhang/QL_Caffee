package com.example.demo.DTOS.SqlDTO;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StoreReportDto {
    private String storeId; // Đổi StoreID -> storeId cho chuẩn naming convention
    private String storeName;
    private String storeAddr;
    private String phoneNum;
    private BigDecimal totalRevenue; // C# decimal? -> Java BigDecimal
    private String status;
}
