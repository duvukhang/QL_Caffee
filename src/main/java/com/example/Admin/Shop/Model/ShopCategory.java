package com.example.Admin.Shop.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "ShopCategory")
@Table(name = "shop_categories", uniqueConstraints = @UniqueConstraint(name = "uk_shop_categories_name", columnNames = "name"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShopCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120, columnDefinition = "nvarchar(120)")
    private String name;

    @Column(length = 500, columnDefinition = "nvarchar(500)")
    private String description;

    @Column(nullable = false)
    private boolean active = true;
}
