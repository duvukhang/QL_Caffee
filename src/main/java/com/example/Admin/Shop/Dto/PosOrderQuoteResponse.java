package com.example.Admin.Shop.Dto;

import java.math.BigDecimal;

public record PosOrderQuoteResponse(
        boolean valid,
        String message,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal totalAmount) {
}
