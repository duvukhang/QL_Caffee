package com.example.Admin.Shop.Model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "ShopUserCoupon")
@Table(
        name = "shop_user_coupons",
        uniqueConstraints = @UniqueConstraint(name = "uk_shop_user_coupons_user_coupon", columnNames = {"user_id", "coupon_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShopUserCoupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private ShopUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private ShopCoupon coupon;

    private boolean used;

    private LocalDateTime assignedAt = LocalDateTime.now();

    private LocalDateTime usedAt;
}
