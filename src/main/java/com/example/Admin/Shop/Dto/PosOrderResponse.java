package com.example.Admin.Shop.Dto;

import java.math.BigDecimal;

public record PosOrderResponse(
        Long orderId,
        String orderCode,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        String paymentStatus,
        String orderStatus,
        String message) {
}
