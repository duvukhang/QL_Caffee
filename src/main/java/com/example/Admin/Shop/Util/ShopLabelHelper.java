package com.example.Admin.Shop.Util;

import org.springframework.stereotype.Component;

@Component("shopLabels")
public class ShopLabelHelper {

    public String label(Object value) {
        if (value == null) {
            return "Chưa có";
        }
        String key = value.toString();
        return switch (key) {
            case "PENDING", "WAITING_CONFIRMATION" -> "Chờ xác nhận";
            case "CONFIRMED" -> "Đã xác nhận";
            case "SHIPPING", "DELIVERING" -> "Đang giao";
            case "COMPLETED", "DELIVERED" -> "Hoàn thành";
            case "CANCELLED", "CANCELED" -> "Đã hủy";
            case "UNPAID" -> "Chưa thanh toán";
            case "PAID" -> "Đã thanh toán";
            case "FAILED" -> "Thanh toán thất bại";
            case "COD" -> "Thanh toán khi nhận hàng";
            case "CARD" -> "Thẻ ngân hàng";
            case "E_WALLET", "WALLET" -> "Ví điện tử";
            case "ACTIVE" -> "Đang hoạt động";
            case "INACTIVE" -> "Ngừng hoạt động";
            case "APPROVED" -> "Đã duyệt";
            case "PENDING_REVIEW" -> "Chờ duyệt";
            case "REJECTED" -> "Từ chối";
            case "PERCENT" -> "Giảm theo phần trăm";
            case "FIXED_AMOUNT" -> "Giảm số tiền";
            case "IMPORT" -> "Nhập kho";
            case "EXPORT" -> "Xuất kho";
            case "CUSTOMER" -> "Khách hàng";
            case "STAFF" -> "Nhân viên";
            case "ADMIN" -> "Quản trị viên";
            case "MANAGER" -> "Quản lý";
            case "SUPER_ADMIN" -> "Quản trị hệ thống";
            default -> prettify(key);
        };
    }

    public String orderBadgeClass(Object value) {
        if (value == null) {
            return "status-muted";
        }
        return switch (value.toString()) {
            case "PENDING", "WAITING_CONFIRMATION" -> "status-pending";
            case "CONFIRMED" -> "status-confirmed";
            case "SHIPPING", "DELIVERING" -> "status-shipping";
            case "COMPLETED", "DELIVERED", "PAID", "ACTIVE", "APPROVED" -> "status-success";
            case "CANCELLED", "CANCELED", "FAILED", "REJECTED", "INACTIVE" -> "status-danger";
            case "UNPAID" -> "status-muted";
            default -> "status-info";
        };
    }

    public String activeLabel(boolean active) {
        return active ? "Đang hoạt động" : "Ngừng hoạt động";
    }

    public String activeBadgeClass(boolean active) {
        return active ? "status-success" : "status-muted";
    }

    public String enabledLabel(boolean enabled) {
        return enabled ? "Đang hoạt động" : "Đã khóa";
    }

    public String reviewLabel(boolean approved) {
        return approved ? "Đã duyệt" : "Chờ duyệt";
    }

    public String couponScopeLabel(boolean publicCoupon) {
        return publicCoupon ? "Công khai" : "Riêng tư";
    }

    private String prettify(String key) {
        return key.replace('_', ' ').toLowerCase();
    }
}
