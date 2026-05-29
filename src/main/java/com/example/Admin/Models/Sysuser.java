package com.example.Admin.Models;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Table(name = "sysuser", schema = "management")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Sysuser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserId")
    private Integer userId;

    @Column(name = "UserName", length = 100, nullable = false, unique = true)
    private String userName;

    @Column(name = "Password", length = 100, nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StaffId")
    private Staff staff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StoreId", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RoleId", nullable = false)
    private Sysrole role;

    // 🛠️ ĐÃ FIX: Đổi từ "sysUser" thành "sysuser" viết thường để khớp 100% với biến thực thể trong Order.java
    @OneToMany(mappedBy = "sysuser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Collection<Order> orders = new ArrayList<>();
}