package com.example.Admin.Models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @Column(name = "ProductId", length = 10, nullable = false, columnDefinition = "char(10)")
    private String productId;

    @Column(name = "ProductName", length = 100, nullable = false)
    private String productName;

    // Trong MyDbContext: entity.Property(e => e.Price).HasColumnType("money");
    @Column(name = "Price", columnDefinition = "money", nullable = false)
    private BigDecimal price;

    @Column(name = "IMG", length = 500)
    private String img;

    @Column(name = "Status", length = 100)
    private String status;

    // Trong MyDbContext: entity.Property(e => e.Decription).HasColumnType("text");
    @Column(name = "Decription", columnDefinition = "text")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SubcategoryId", nullable = false)
    private SubCategory subcategory;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Collection<OrderDetail> orderDetails = new ArrayList<>();

    public String getDecription() {
        return description;
    }

    public void setDecription(String decription) {
        this.description = decription;
    }

    public String getSubcategoryId() {
        return subcategory == null ? null : subcategory.getSubCategory();
    }
}
