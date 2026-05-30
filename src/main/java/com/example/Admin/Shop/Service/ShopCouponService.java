package com.example.Admin.Shop.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Admin.Shop.Model.DiscountType;
import com.example.Admin.Shop.Model.ShopCoupon;
import com.example.Admin.Shop.Model.ShopUser;
import com.example.Admin.Shop.Repository.ShopCouponRepository;
import com.example.Admin.Shop.Repository.ShopUserCouponRepository;

@Service
public class ShopCouponService {
    private final ShopCouponRepository couponRepository;
    private final ShopUserCouponRepository userCouponRepository;

    public ShopCouponService(ShopCouponRepository couponRepository, ShopUserCouponRepository userCouponRepository) {
        this.couponRepository = couponRepository;
        this.userCouponRepository = userCouponRepository;
    }

    public CouponQuote quote(String code, ShopUser user, BigDecimal subtotal) {
        if (code == null || code.isBlank()) {
            return CouponQuote.empty(subtotal);
        }

        ShopCoupon coupon = couponRepository.findByCodeIgnoreCase(code.trim())
                .orElse(null);
        if (coupon == null) {
            return CouponQuote.invalid("Mã khuyến mãi không tồn tại", subtotal);
        }

        LocalDateTime now = LocalDateTime.now();
        if (!coupon.isActive()) {
            return CouponQuote.invalid("Mã khuyến mãi đang tắt", subtotal);
        }
        if (now.isBefore(coupon.getStartDate())) {
            return CouponQuote.invalid("Mã khuyến mãi chưa đến ngày sử dụng", subtotal);
        }
        if (now.isAfter(coupon.getEndDate())) {
            return CouponQuote.invalid("Mã khuyến mãi đã hết hạn", subtotal);
        }
        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            return CouponQuote.invalid("Mã khuyến mãi đã hết lượt dùng", subtotal);
        }
        if (subtotal.compareTo(coupon.getMinOrderAmount()) < 0) {
            return CouponQuote.invalid("Đơn hàng chưa đạt giá trị tối thiểu", subtotal);
        }
        if (!coupon.isPublicCoupon()) {
            var assigned = userCouponRepository.findByUserAndCoupon(user, coupon);
            if (assigned.isEmpty()) {
                return CouponQuote.invalid("Mã này không thuộc tài khoản của bạn", subtotal);
            }
            if (assigned.get().isUsed()) {
                return CouponQuote.invalid("Bạn đã dùng mã này rồi", subtotal);
            }
        }

        BigDecimal discount = coupon.getDiscountType() == DiscountType.PERCENT
                ? subtotal.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : coupon.getDiscountValue();
        if (coupon.getMaxDiscountAmount() != null && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
            discount = coupon.getMaxDiscountAmount();
        }
        if (discount.compareTo(subtotal) > 0) {
            discount = subtotal;
        }
        return new CouponQuote(true, "Áp dụng mã thành công", coupon, discount, subtotal.subtract(discount));
    }

    @Transactional
    public void markUsed(ShopCoupon coupon, ShopUser user) {
        if (coupon == null) {
            return;
        }
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);
        userCouponRepository.findByUserAndCoupon(user, coupon).ifPresent(userCoupon -> {
            userCoupon.setUsed(true);
            userCoupon.setUsedAt(LocalDateTime.now());
            userCouponRepository.save(userCoupon);
        });
    }

    public record CouponQuote(boolean valid, String message, ShopCoupon coupon, BigDecimal discountAmount, BigDecimal totalAmount) {
        static CouponQuote empty(BigDecimal subtotal) {
            return new CouponQuote(true, "", null, BigDecimal.ZERO, subtotal);
        }

        static CouponQuote invalid(String message, BigDecimal subtotal) {
            return new CouponQuote(false, message, null, BigDecimal.ZERO, subtotal);
        }
    }
}
