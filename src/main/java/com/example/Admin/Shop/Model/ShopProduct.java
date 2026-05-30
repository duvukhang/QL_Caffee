package com.example.Admin.Shop.Model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "ShopProduct")
@Table(name = "shop_products", uniqueConstraints = @UniqueConstraint(name = "uk_shop_products_slug", columnNames = "slug"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShopProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180, columnDefinition = "nvarchar(180)")
    private String name;

    @Column(nullable = false, length = 220, columnDefinition = "nvarchar(220)")
    private String slug;

    @Column(columnDefinition = "nvarchar(max)")
    private String description;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal salePrice;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean featured;

    @Column(nullable = false)
    private boolean newProduct;

    @Column(nullable = false)
    private boolean saleProduct;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ShopCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private ShopBrand brand;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShopProductImage> images = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public BigDecimal getEffectivePrice() {
        return isOnSale() ? salePrice : price;
    }

    public boolean isOnSale() {
        return salePrice != null && salePrice.compareTo(BigDecimal.ZERO) > 0 && salePrice.compareTo(price) < 0;
    }

    public String getMainImagePath() {
        return images.stream()
                .filter(ShopProductImage::isMainImage)
                .findFirst()
                .or(() -> images.stream().findFirst())
                .map(ShopProductImage::getImagePath)
                .orElse("/img/no-image.png");
    }
}
