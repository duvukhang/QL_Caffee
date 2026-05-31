package com.example.Admin.Shop.Util;

import org.springframework.stereotype.Component;

import com.example.Admin.Shop.Model.PaymentMethod;
import com.example.Admin.Shop.Model.PaymentStatus;
import com.example.Admin.Shop.Model.ShopOrderStatus;
import com.example.Admin.Shop.Model.ShopOrderType;

@Component("shopLabels")
public class ShopLabelHelper {

    public String label(Object value) {
        if (value == null) {
            return "Chưa có";
        }
        if (value instanceof PaymentStatus status) {
            return switch (status) {
                case UNPAID -> "Chưa thanh toán";
                case PENDING -> "Chờ thanh toán";
                case PAID -> "Đã thanh toán";
                case FAILED -> "Thanh toán thất bại";
            };
        }
        if (value instanceof PaymentMethod method) {
            return switch (method) {
                case COD -> "Thanh toán khi nhận hàng";
                case CASH -> "Tiền mặt";
                case BANK_QR -> "Chuyển khoản QR";
                case BANK_QR_MANUAL -> "Chuyển khoản QR";
                case CARD -> "Thẻ ngân hàng";
                case WALLET -> "Ví điện tử";
            };
        }
        if (value instanceof ShopOrderType type) {
            return switch (type) {
                case ONLINE -> "Đơn online";
                case POS -> "Đơn tại quầy";
                case TAKE_AWAY -> "Mang đi";
            };
        }
        if (value instanceof ShopOrderStatus status) {
            return switch (status) {
                case PENDING_PAYMENT -> "Chờ thanh toán";
                case PENDING -> "Chờ xác nhận";
                case CONFIRMED -> "Đã xác nhận";
                case SHIPPING -> "Đang giao";
                case COMPLETED -> "Hoàn thành";
                case CANCELLED -> "Đã hủy";
            };
        }

        String key = value.toString();
        return switch (key) {
            case "WAITING_CONFIRMATION" -> "Chờ xác nhận";
            case "DELIVERING" -> "Đang giao";
            case "DELIVERED" -> "Hoàn thành";
            case "CANCELED" -> "Đã hủy";
            case "E_WALLET" -> "Ví điện tử";
            case "BANK_QR" -> "Chuyển khoản QR";
            case "ACTIVE" -> "Đang hoạt động";
            case "INACTIVE" -> "Ngừng hoạt động";
            case "APPROVED" -> "Đã duyệt";
            case "PENDING_REVIEW" -> "Chờ duyệt";
            case "REJECTED" -> "Từ chối";
            case "PERCENT" -> "Giảm theo phần trăm";
            case "FIXED_AMOUNT" -> "Giảm số tiền";
            case "IMPORT" -> "Nhập kho";
            case "EXPORT" -> "Xuất kho";
            case "ORDER_CANCEL" -> "Hoàn kho đơn hủy";
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
            case "PENDING", "PENDING_PAYMENT", "WAITING_CONFIRMATION" -> "status-pending";
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
