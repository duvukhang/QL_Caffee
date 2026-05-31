package com.example.Admin.Shop.Dto;

import java.util.List;

import com.example.Admin.Shop.Model.PaymentMethod;
import com.example.Admin.Shop.Model.ShopOrderType;

public record PosOrderRequest(
        List<PosOrderItemRequest> items,
        String couponCode,
        PaymentMethod paymentMethod,
        ShopOrderType orderType) {
}
