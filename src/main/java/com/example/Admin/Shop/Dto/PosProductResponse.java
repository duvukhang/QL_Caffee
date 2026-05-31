package com.example.Admin.Shop.Dto;

import java.math.BigDecimal;

public record PosProductResponse(
        Long id,
        String name,
        String category,
        BigDecimal price,
        int quantity,
        String imagePath) {
}
