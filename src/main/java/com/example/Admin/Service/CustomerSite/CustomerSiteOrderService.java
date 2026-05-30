package com.example.Admin.Service.CustomerSite;

import com.example.Admin.DTOS.Request.HoaDonRequest;
import com.example.Admin.Models.Cart;
import com.example.Admin.Models.CartItem;
import com.example.Admin.Models.Order;
import com.example.Admin.Service.Customer.CustomerOrderService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerSiteOrderService {

    private final CustomerOrderService customerOrderService;

    public CustomerSiteOrderService(CustomerOrderService customerOrderService) {
        this.customerOrderService = customerOrderService;
    }

    public Order createOrder(Cart cart, String customerId) {
        if (cart == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Gi\u1ecf h\u00e0ng \u0111ang tr\u1ed1ng.");
        }

        List<HoaDonRequest.ProductItem> items = cart.getItems().stream()
                .map(this::toProductItem)
                .toList();
        return customerOrderService.createOrder(customerId, items);
    }

    public void cancelOrder(String orderId, String customerId) {
        customerOrderService.cancelOrder(orderId, customerId);
    }

    private HoaDonRequest.ProductItem toProductItem(CartItem cartItem) {
        HoaDonRequest.ProductItem item = new HoaDonRequest.ProductItem();
        item.setMasp(cartItem.getProductId());
        item.setSoLuong(cartItem.getQuantity());
        return item;
    }
}
