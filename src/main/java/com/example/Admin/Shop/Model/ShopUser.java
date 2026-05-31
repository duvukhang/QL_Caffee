package com.example.Admin.Shop.Model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "ShopUser")
@Table(
        name = "shop_users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_shop_users_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_shop_users_email", columnNames = "email")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShopUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60, columnDefinition = "nvarchar(60)")
    private String username;

    @Column(nullable = false, length = 120, columnDefinition = "nvarchar(120)")
    private String email;

    @Column(nullable = false, length = 100, columnDefinition = "nvarchar(100)")
    private String passwordHash;

    @Column(nullable = false, length = 120, columnDefinition = "nvarchar(120)")
    private String fullName;

    @Column(length = 20, columnDefinition = "nvarchar(20)")
    private String phone;

    @Column(length = 300, columnDefinition = "nvarchar(300)")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShopRole role = ShopRole.CUSTOMER;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public boolean isAdminLike() {
        return role != null && role.isEmployee();
    }
}
