package com.example.Admin.DTOS.Request;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class HoaDonRequest {
    
    private List<ProductItem> dssp = new ArrayList<>();
    private String makhach;

    // Nested class tương đương C#
    @Data
    public static class ProductItem {
        private String masp;
        private Integer soLuong;
    }
}
