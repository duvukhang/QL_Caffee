package com.example.Admin.Service.Customer;

import com.example.Admin.DTOS.Request.HoaDonRequest;
import com.example.Admin.Models.Order;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerOrderService {

    private static final String STATUS_RECEIVED = "Ti\u1ebfp nh\u1eadn";
    private static final String STATUS_CANCELLED = "\u0110\u00e3 h\u1ee7y";

    private final JdbcTemplate jdbcTemplate;

    public CustomerOrderService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public Order createOrder(String customerId, List<HoaDonRequest.ProductItem> items) {
        String normalizedCustomerId = normalize(customerId);
        if (normalizedCustomerId == null) {
            throw new IllegalArgumentException("Khong tim thay khach hang dang dang nhap.");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Don hang chua co san pham.");
        }

        Integer customerCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM customers.Customer WHERE CustomerId = ?",
                Integer.class,
                normalizedCustomerId
        );
        if (customerCount == null || customerCount == 0) {
            throw new IllegalArgumentException("Khong tim thay khach hang trong database.");
        }

        String orderId = generateOrderId();
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update(
                "INSERT INTO dbo.orders (OrderId, Status, RecivingDate, UpdateStatusDate, CustomerId) VALUES (?, ?, ?, ?, ?)",
                orderId,
                STATUS_RECEIVED,
                now,
                now,
                normalizedCustomerId
        );

        for (HoaDonRequest.ProductItem item : items) {
            String productId = normalize(item.getMasp());
            Integer quantity = item.getSoLuong();
            if (productId == null) {
                throw new IllegalArgumentException("Ma san pham khong hop le.");
            }
            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("So luong san pham phai lon hon 0.");
            }

            Integer productCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(1) FROM dbo.products WHERE ProductId = ?",
                    Integer.class,
                    productId
            );
            if (productCount == null || productCount == 0) {
                throw new IllegalArgumentException("Khong tim thay san pham: " + productId);
            }

            jdbcTemplate.update(
                    "INSERT INTO dbo.order_detail (OrderId, ProductId, Quantity) VALUES (?, ?, ?)",
                    orderId,
                    productId,
                    quantity
            );
        }

        Order order = new Order();
        order.setOrderId(orderId);
        order.setStatus(STATUS_RECEIVED);
        order.setRecivingDate(now);
        order.setUpdateStatusDate(now);
        order.setCustomerId(normalizedCustomerId);
        return order;
    }

    @Transactional
    public void cancelOrder(String orderId, String customerId) {
        String normalizedOrderId = normalize(orderId);
        String normalizedCustomerId = normalize(customerId);
        if (normalizedOrderId == null || normalizedCustomerId == null) {
            throw new IllegalArgumentException("Thong tin huy don khong hop le.");
        }

        int updated = jdbcTemplate.update(
                "UPDATE dbo.orders SET Status = ?, UpdateStatusDate = ? WHERE OrderId = ? AND CustomerId = ? AND Status = ?",
                STATUS_CANCELLED,
                LocalDateTime.now(),
                normalizedOrderId,
                normalizedCustomerId,
                STATUS_RECEIVED
        );
        if (updated == 0) {
            throw new IllegalArgumentException("Chi duoc huy don khi don dang o trang thai Tiep nhan.");
        }
    }

    private String generateOrderId() {
        for (int i = 0; i < 200; i++) {
            String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            String orderId = "DH" + suffix;
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(1) FROM dbo.orders WHERE OrderId = ?",
                    Integer.class,
                    orderId
            );
            if (count == null || count == 0) {
                return orderId;
            }
        }
        throw new IllegalStateException("Khong tao duoc ma don hang, vui long thu lai.");
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
